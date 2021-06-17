package com.yiliao.control;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.servlet.http.HttpServletRequest;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yiliao.service.AlbumService;
import com.yiliao.util.BaseUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.RSACoderUtil;

import net.sf.json.JSONObject;

@Controller
@RequestMapping("app")
public class AlbumControl {

	@Autowired
	private AlbumService albumService;

	/**
	 * 获取他人相册
	 *
	 * @param userId
	 * @param page
	 * @param response
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	@Deprecated
	@RequestMapping(value = { "getDynamicList" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getDynamicList(HttpServletRequest req) throws NoSuchAlgorithmException, InvalidKeySpecException {

		// 进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("coverUserId"), param.getInt("page"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.albumService.getDynamicList(param.getInt("userId"), param.getInt("coverUserId"),
				param.getInt("page"),null);

	}

	/**
	 * 获取他人相册
	 * 
	 */
	@RequestMapping(value = { "getAlbumList" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getAlbumList(HttpServletRequest req) {

		// 进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("coverUserId"), param.getInt("page"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.albumService.getDynamicList(param.getInt("userId"), param.getInt("coverUserId"),
				param.getInt("page"),param.has("fileType") ? param.getInt("fileType") : null);
	}

	/**
	 * 获取我的相册用户的相册
	 * 
	 * @param userId
	 * @param page
	 * @param response
	 */
	@Deprecated
	@RequestMapping(value = { "getMyPhotoList" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getMyPhotoList(HttpServletRequest req) {

		// 进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("page"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.albumService.getMyPhotoList(param.getInt("userId"), param.getInt("page"));

	}

	/**
	 * 上传至相册
	 * 
	 * @param userId    用户编号
	 * @param t_title   标题
	 * @param video_img 封面图片(视频)
	 * @param fileId    文件编号(视频)
	 * @param url       文件地址
	 * @param type      文件类型(0.图片 1.视频)
	 * @param gold      金币
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "addMyPhotoAlbum" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil addMyPhotoAlbum(HttpServletRequest req) {
		// 进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"))
				|| (StringUtils.isBlank(param.getOrDefault("video_img","").toString()) && StringUtils.isBlank(param.getOrDefault("url","").toString()))
				|| (param.getInt("type") > 1 || param.getInt("type") < 0)) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.albumService.addMyPhotoAlbum(param.getInt("userId"), param.getString("t_title"),
				param.getOrDefault("video_img","").toString(), param.getOrDefault("fileId","").toString(), param.getString("url"), param.getInt("type"),
				param.getInt("gold"));

	}

	/**
	 * 删除我的照片
	 * 
	 * @param photoId
	 * @param response
	 */
	@RequestMapping(value = { "delMyPhoto" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil delMyPhoto(HttpServletRequest req) {
		// 进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("photoId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.albumService.delMyPhoto(param.getInt("photoId"),param.getInt("userId"));
	}

	/**
	 * 1.4版获取我的相册(年度)
	 * 
	 * @param userId
	 * @param response
	 */
	@RequestMapping(value = { "getMyAnnualAlbum" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getMyAnnualAlbum(HttpServletRequest req) {

		// 进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getInt("userId"), param.getInt("page"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		int type = -1;
		if(!StrUtil.hasEmpty(param.getString("type"))){
			type = Integer.parseInt(param.getString("type"));
		}

		return this.albumService.getMyAnnualAlbum(param.getInt("userId"), type, param.getInt("page"));
	}

	/**
	 * 获取我的相册列表(月,分页)
	 * 
	 * @param userId
	 * @param year
	 * @param month
	 * @param response
	 */
	@RequestMapping(value = { "getMyMonthAlbum" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getMyMonthAlbum(HttpServletRequest req) {

		// 进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getInt("userId"), param.getInt("year"), param.getInt("month"),
				param.getInt("page"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.albumService.getMyMonthAlbum(param.getInt("userId"), param.getInt("year"), param.getInt("month"),
				param.getInt("page"));

	}
	
	/**
	 * 设置封面视频
	 * 
	 * @param userId
	 * @param year
	 * @param month
	 * @param response
	 */
	@RequestMapping(value = { "setFirstAlbum" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil setFirstAlbum(HttpServletRequest req) {
		// 进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getInt("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.albumService.setFirstAlbum(param.getInt("userId"), param.getInt("albumId"));

	}
}
