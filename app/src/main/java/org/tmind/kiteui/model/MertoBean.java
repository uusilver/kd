/**   
 * Copyright © 2014 All rights reserved.
 * 
 * @Title: MertoBean.java 
 * @Prject: MetroMain
 * @Package: com.example.metromain 
 * @Description: TODO
 * @author: raot raotao.bj@cabletech.com.cn 
 * @date: 2014年9月25日 下午4:30:16 
 * @version: V1.0   
 */
package org.tmind.kiteui.model;

/**
 * @ClassName: MertoBean
 * @Description: TODO
 * @author: raot raotao.bj@cabletech.com.cn
 * @date: 2014年9月25日 下午4:30:16
 */
public class MertoBean {

	private int iconId;
	private String name;

	public MertoBean() {

	}

	public MertoBean(MertoBean bean) {
		this.iconId = bean.getIconId();
		this.name = bean.getName();
	}

	/**
	 * @return the iconId
	 */
	public int getIconId() {
		return iconId;
	}

	/**
	 * @param iconId
	 *            the iconId to set
	 */
	public void setIconId(int iconId) {
		this.iconId = iconId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}
