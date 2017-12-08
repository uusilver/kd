package org.tmind.kiteui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.tmind.kiteui.utils.AudioRecoderUtils;
import org.tmind.kiteui.utils.PopupWindowFactory;
import org.tmind.kiteui.utils.TimeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MicRecordActivity extends Activity implements View.OnClickListener {

    static final int VOICE_REQUEST_CODE = 66;
    private final static String FILE_PATH = Environment.getExternalStorageDirectory() + "/record/";
    private final static String TAG = "MicRecordActivity";

    private Button mButton;
    private ImageView mImageView;
    private TextView mTextView;
    private AudioRecoderUtils mAudioRecoderUtils;
    private Context context;
    private PopupWindowFactory mPop;
    private LinearLayout rl;

    private ListView listview;
    private List<String> array = new ArrayList<String>();
    private List<String> selectid = new ArrayList<String>();
    private boolean isMulChoice = false; //是否多选
    private MicListAdapter micListAdapter;
    private RelativeLayout layout;
    private Button cancle, delete;
    private TextView txtcount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mic_record);
        context = this;

        rl = (LinearLayout) findViewById(R.id.activity_mic_record);

        mButton = (Button) findViewById(R.id.button);

        //PopupWindow的布局文件
        final View view = View.inflate(this, R.layout.layout_microphone, null);

        mPop = new PopupWindowFactory(this, view);

        //PopupWindow布局文件里面的控件
        mImageView = (ImageView) view.findViewById(R.id.iv_recording_icon);
        mTextView = (TextView) view.findViewById(R.id.tv_recording_time);

        mAudioRecoderUtils = new AudioRecoderUtils();

        //录音回调
        mAudioRecoderUtils.setOnAudioStatusUpdateListener(new AudioRecoderUtils.OnAudioStatusUpdateListener() {

            //录音中....db为声音分贝，time为录音时长
            @Override
            public void onUpdate(double db, long time) {
                mImageView.getDrawable().setLevel((int) (3000 + 6000 * db / 100));
                mTextView.setText(TimeUtils.long2String(time));
            }

            //录音结束，filePath为保存路径
            @Override
            public void onStop(String filePath) {
                Toast.makeText(MicRecordActivity.this, "录音保存在：" + filePath, Toast.LENGTH_SHORT).show();
                mTextView.setText(TimeUtils.long2String(0));
                //clean
                String[] createdAmrFilePathPool = filePath.split("\\/");
                String createdAmrFileName = createdAmrFilePathPool[createdAmrFilePathPool.length - 1];
                array.add(createdAmrFileName);
                //reload
                micListAdapter = new MicListAdapter(context, txtcount);
                listview.setAdapter(micListAdapter);
            }
        });

        //6.0以上需要权限申请
        requestPermissions();

        //读取录音文件
        listview = (ListView) findViewById(R.id.list);
        layout = (RelativeLayout) findViewById(R.id.relative);
        txtcount = (TextView) findViewById(R.id.txtcount);
        cancle = (Button) findViewById(R.id.cancle);
        delete = (Button) findViewById(R.id.delete);
        cancle.setOnClickListener(this);
        delete.setOnClickListener(this);
        initAmrList();
        micListAdapter = new MicListAdapter(context, txtcount);
        listview.setAdapter(micListAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancle:
                isMulChoice = false;
                selectid.clear();
                micListAdapter = new MicListAdapter(context, txtcount);
                listview.setAdapter(micListAdapter);
                layout.setVisibility(View.INVISIBLE);
                break;
            case R.id.delete:
                isMulChoice = false;
                for (int i = 0; i < selectid.size(); i++) {
                    for (int j = 0; j < array.size(); j++) {
                        if (selectid.get(i).equals(array.get(j))) {
                            // TODO delete file, do we need a alert window?
                            String filePath = FILE_PATH + array.get(i);
                            Log.d(TAG, filePath);
                            deleteAmrFile(filePath);
                            array.remove(j);
                        }
                    }
                }
                selectid.clear();
                micListAdapter = new MicListAdapter(context, txtcount);
                listview.setAdapter(micListAdapter);
                layout.setVisibility(View.INVISIBLE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("操作");
    }

    /**
     * 请求权限回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == VOICE_REQUEST_CODE) {
            if ((grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                StartListener();
            } else {
                Toast.makeText(context, "已拒绝权限！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void StartListener() {
        //Button的touch监听
        mButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mPop.showAtLocation(rl, Gravity.CENTER, 0, 0);
                        mButton.setText("松开保存");
                        mAudioRecoderUtils.startRecord();
                        break;
                    case MotionEvent.ACTION_UP:
                        mAudioRecoderUtils.stopRecord();        //结束录音（保存录音文件）
//                        mAudioRecoderUtils.cancelRecord();    //取消录音（不保存录音文件）
                        mPop.dismiss();
                        mButton.setText("按住说话");
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 初始化录音列表
     */
    private void initAmrList() {
        File[] files = new File(FILE_PATH).listFiles();
        for (File file : files) {
            array.add(file.getName());
        }
    }
    /**
     * 开启扫描之前判断权限是否打开
     */
    private void requestPermissions() {
        //判断是否开启摄像头权限
        if ((ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
                ) {
            StartListener();

            //判断是否开启语音权限
        } else {
            //请求获取摄像头权限
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, VOICE_REQUEST_CODE);
        }

    }

    /**
     * 获取文件类型
     * @param f
     * @return
     */
    private String getMIMEType(File f) {
        String end = f.getName().substring(
                f.getName().lastIndexOf(".") + 1, f.getName().length())
                .toLowerCase();
        String type = "";
        if (end.equals("mp3") || end.equals("aac") || end.equals("aac")
                || end.equals("amr") || end.equals("mpeg")
                || end.equals("mp4")) {
            type = "audio";
        } else if (end.equals("jpg") || end.equals("gif")
                || end.equals("png") || end.equals("jpeg")) {
            type = "image";
        } else {
            type = "*";
        }
        type += "/*";
        return type;
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    private boolean deleteAmrFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * @author ieasy360_1
     *         自定义Adapter
     */
    class MicListAdapter extends BaseAdapter {
        private Context context;
        private LayoutInflater inflater = null;
        private HashMap<Integer, View> mView;
        public HashMap<Integer, Integer> visiblecheck;//用来记录是否显示checkBox
        public HashMap<Integer, Boolean> ischeck;
        private TextView txtcount;

        public MicListAdapter(Context context, TextView txtcount) {
            this.context = context;
            this.txtcount = txtcount;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mView = new HashMap<Integer, View>();
            visiblecheck = new HashMap<Integer, Integer>();
            ischeck = new HashMap<Integer, Boolean>();
            if (isMulChoice) {
                for (int i = 0; i < array.size(); i++) {
                    ischeck.put(i, false);
                    visiblecheck.put(i, CheckBox.VISIBLE);
                }
            } else {
                for (int i = 0; i < array.size(); i++) {
                    ischeck.put(i, false);
                    visiblecheck.put(i, CheckBox.INVISIBLE);
                }
            }
        }

        public int getCount() {
            return array.size();
        }

        public Object getItem(int position) {
            return array.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = mView.get(position);
            if (view == null) {
                view = inflater.inflate(R.layout.mic_list_item, null);
                TextView txt = (TextView) view.findViewById(R.id.txtName);
                final CheckBox ceb = (CheckBox) view.findViewById(R.id.check);
                txt.setText(array.get(position));
                ceb.setChecked(ischeck.get(position));
                ceb.setVisibility(visiblecheck.get(position));
                view.setOnLongClickListener(new Onlongclick());
                view.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (isMulChoice) {
                            if (ceb.isChecked()) {
                                ceb.setChecked(false);
                                selectid.remove(array.get(position));
                            } else {
                                ceb.setChecked(true);
                                selectid.add(array.get(position));
                            }
                            txtcount.setText("共选择了" + selectid.size() + "项");
                        } else {
                            //TODO play AMR file
                            File playFile = new File(FILE_PATH + array.get(position));
                            Intent intent = new Intent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setAction(Intent.ACTION_VIEW);
                            String type = getMIMEType(playFile);
                            intent.setDataAndType(Uri.fromFile(playFile), type);
                            startActivity(intent);
                        }
                    }
                });
                mView.put(position, view);
            }
            return view;
        }
    }

    class Onlongclick implements View.OnLongClickListener {
        public boolean onLongClick(View v) {
            isMulChoice = true;
            selectid.clear();
            layout.setVisibility(View.VISIBLE);
            for (int i = 0; i < array.size(); i++) {
                micListAdapter.visiblecheck.put(i, CheckBox.VISIBLE);
            }
            micListAdapter = new MicListAdapter(context, txtcount);
            listview.setAdapter(micListAdapter);
            return true;
        }
    }
}
