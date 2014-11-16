package com.saituo.order.service.account;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.saituo.order.commons.SessionVariable;
import com.saituo.order.commons.VariableUtils;
import com.saituo.order.commons.enumeration.entity.PortraitSize;
import com.saituo.order.commons.enumeration.entity.ResourceType;
import com.saituo.order.commons.page.Page;
import com.saituo.order.commons.page.PageRequest;
import com.saituo.order.commons.utils.HexPassword;
import com.saituo.order.commons.valid.annotation.MapValid;
import com.saituo.order.dao.account.GroupDao;
import com.saituo.order.dao.account.MenuDao;
import com.saituo.order.dao.account.UserDao;
import com.saituo.order.service.ServiceException;

/**
 * 账户业务逻辑
 */
@Service
@Transactional
public class AccountService {

	@Autowired
	private UserDao userDao;

	@Autowired
	private GroupDao groupDao;

	@Autowired
	private MenuDao menuDao;

	/**
	 * 默认的用户上传头像的文件夹路径
	 */
	public static final String DEFAULT_USER_UPLOAD_PORTRAIT_PATH = "." + File.separator + "upload_portrait"
			+ File.separator;

	// ----------------------------------- 用户管理
	// ----------------------------------------//

	/**
	 * 获取用户
	 * 
	 * @param id
	 *            用户主键 ID
	 * 
	 * @return 用户实体 Map
	 */
	public Map<String, Object> getUser(String id) {
		return userDao.get(id);
	}

	/**
	 * 获取当前用户头像
	 * 
	 * @param portraitSize
	 *            头像尺寸枚举
	 * 
	 * @throws IOException
	 * 
	 * @return 头像二进制 byte 数组
	 * 
	 */
	public byte[] getCurrentUserPortrait(PortraitSize portraitSize) throws IOException {

		Map<String, Object> entity = SessionVariable.getCurrentSessionVariable().getUser();
		String path = DEFAULT_USER_UPLOAD_PORTRAIT_PATH + entity.get("id") + File.separator + portraitSize.getName();
		File f = new File(path);
		if (!f.exists()) {
			return new byte[0];
		}

		return FileUtils.readFileToByteArray(new File(path));
	}

	/**
	 * 保存用户
	 * 
	 * @param entity
	 *            用户实体 Map
	 * @param groupIds
	 *            关联的组主键 ID 集合
	 */
	public void saveUser(@MapValid("user") Map<String, Object> entity) {
		if (entity.containsKey("id")) {
			updateUser(entity);
		}
	}

	/**
	 * 更新用户
	 * 
	 * @param entity
	 *            用户实体 Map
	 * @param groupIds
	 *            关联的组主键 ID 集合，如果为 null 或 size 等于 0 表示删除所有关联
	 */
	@CacheEvict(value = "shiroAuthenticationCache", key = "#entity.get('username')")
	private void updateUser(Map<String, Object> entity) {
		userDao.update(entity);
	}

	/**
	 * 更新用户密码
	 * 
	 * @param entity
	 *            用户实体 Map
	 * @param oldPassword
	 *            当前密码
	 * @param newPassword
	 *            新imia
	 */
	@CacheEvict(value = "shiroAuthenticationCache", key = "#entity.get('username')")
	public void updateUserPassword(Map<String, Object> entity, String oldPassword, String newPassword) {

		String oldPasswordEntrypt = HexPassword.entryptPassword(oldPassword);

		if (!StringUtils.equals(entity.get("password").toString(), oldPasswordEntrypt)) {
			throw new ServiceException("当前密码不正确");
		}

		if (StringUtils.isEmpty(newPassword)) {
			throw new ServiceException("新密码不能为空");
		}

		String newPasswordEntrypt = HexPassword.entryptPassword(newPassword);
		String id = VariableUtils.typeCast(entity.get("id"), String.class);
		userDao.updatePassword(id, newPasswordEntrypt);
	}

	/**
	 * 更新用户头像
	 * 
	 * @param is
	 *            头像输入流
	 * 
	 * @throws IOException
	 */
	public void updateUserPortrait(Map<String, Object> entity, InputStream is) throws IOException {

		File uploadPortrait = new File(DEFAULT_USER_UPLOAD_PORTRAIT_PATH);

		if (!uploadPortrait.exists()) {
			uploadPortrait.mkdirs();
		}

		File file = new File(DEFAULT_USER_UPLOAD_PORTRAIT_PATH + entity.get("id") + File.separator);

		if (!file.exists() || !file.isDirectory()) {
			file.deleteOnExit();
			file.mkdirs();
		}

		String portraitPath = file.getAbsolutePath() + File.separator;
		String originalPicPath = portraitPath + PortraitSize.BIG.getName();

		FileOutputStream fileOutputStream = new FileOutputStream(originalPicPath);
		IOUtils.copy(is, fileOutputStream);
		fileOutputStream.close();

		scaleImage(originalPicPath, portraitPath, PortraitSize.MIDDLE);
		scaleImage(originalPicPath, portraitPath, PortraitSize.SMALL);
	}

	/**
	 * 辅助方法，缩小图片像素
	 * 
	 * @param sourcePath
	 *            源图片路径
	 * @param targetPath
	 *            缩小后的图片路径
	 * @param portraitSize
	 *            头像尺寸
	 * 
	 * @throws IOException
	 */
	private String scaleImage(String sourcePath, String targetPath, PortraitSize portraitSize) throws IOException {
		InputStream inputStream = new FileInputStream(sourcePath);

		BufferedImage source = ImageIO.read(inputStream);
		ColorModel targetColorModel = source.getColorModel();

		inputStream.close();

		int width = portraitSize.getWidth();
		int height = portraitSize.getHeight();

		BufferedImage target = new BufferedImage(targetColorModel, targetColorModel.createCompatibleWritableRaster(
				width, height), targetColorModel.isAlphaPremultiplied(), null);

		Image scaleImage = source.getScaledInstance(width, height, Image.SCALE_SMOOTH);

		Graphics2D g = target.createGraphics();
		g.drawImage(scaleImage, 0, 0, width, height, null);
		g.dispose();

		String result = targetPath + portraitSize.getName();
		FileOutputStream fileOutputStream = new FileOutputStream(result);
		ImageIO.write(target, "jpeg", fileOutputStream);

		fileOutputStream.close();

		return result;
	}

	/**
	 * 获取用户
	 * 
	 * @param usernameOrEmail
	 *            登录帐号或电子邮件
	 * 
	 * @return 用户实体 Map
	 */
	public Map<String, Object> getUserByUsernameOrEmail(String usernameOrEmail) {
		return userDao.getByUsernameOrEmail(usernameOrEmail);
	}

	/**
	 * 判断用户登录帐号是否唯一
	 * 
	 * @param username
	 *            登录帐号
	 * 
	 * @return true 表示唯一，否则 false
	 */
	public boolean isUsernameUnique(String username) {
		return getUserByUsernameOrEmail(username) == null;
	}

	/**
	 * 判断用户电子邮件是否唯一
	 * 
	 * @param email
	 *            电子邮件
	 * 
	 * @return true 表示唯一，否则 false
	 */
	public boolean isUserEmailUnique(String email) {
		return getUserByUsernameOrEmail(email) == null;
	}

	/**
	 * 查询用户
	 * 
	 * @param filter
	 *            查询条件
	 * 
	 * @return 用户实体 Map 集合
	 */
	public List<Map<String, Object>> findUsers(Map<String, Object> filter) {
		return userDao.find(filter);
	}

	/**
	 * 查询用户
	 * 
	 * @param pageRequest
	 *            分页请求参数
	 * @param filter
	 *            查询条件
	 * 
	 * @return 用户实体 Map 的分页对象
	 */
	public Page<Map<String, Object>> findUsers(PageRequest pageRequest, Map<String, Object> filter) {
		long total = userDao.count(filter);
		filter.putAll(pageRequest.getMap());
		List<Map<String, Object>> content = findUsers(filter);
		return new Page<Map<String, Object>>(pageRequest, content, total);
	}

	// ----------------------------------- 组管理
	// ----------------------------------------//

	/**
	 * 获取组
	 * 
	 * @param id
	 *            组主键 ID
	 * 
	 * @return 组实体 Map
	 */
	public Map<String, Object> getGroup(String id) {
		return groupDao.get(id);
	}

	/**
	 * 获取用户所在的组
	 * 
	 * @param userId
	 *            用户主键 ID
	 * 
	 * @return 组实体 Map 集合
	 */
	public List<Map<String, Object>> getUserGroups(String userId) {
		return groupDao.getUserGroups(userId);
	}

	/**
	 * 查询组
	 * 
	 * @param filter
	 *            查询条件
	 * 
	 * @return 组实体 Map 集合
	 */
	public List<Map<String, Object>> findGroups(Map<String, Object> filter) {
		return groupDao.find(filter);
	}

	/**
	 * 获取菜单
	 * 
	 * @param id
	 *            资源主键 ID
	 * 
	 * @return 资源实体 Map
	 */
	public Map<String, Object> getMenu(String id) {
		return menuDao.get(id);
	}

	/**
	 * 获取所有菜单
	 * 
	 * @return 资源实体 Map 集合
	 */
	public List<Map<String, Object>> getMenus(String... ignore) {
		return menuDao.getAll(ignore);
	}

	/**
	 * 获取用户资源
	 * 
	 * @param userId
	 *            用户主键 ID
	 * 
	 * @return 资源实体 Map 集合
	 */
	public List<Map<String, Object>> getUserMenus(String userId) {
		return menuDao.getUserMenus(userId);
	}

	/**
	 * 合并资源，要获取资源的父类通过 "parent" key 来获取，如果不存在 "parent" key 表示该资源 Map 为根节点，
	 * 要获取资源的子类通过 "children" key 来获取
	 * 
	 * @param resources
	 *            要合并的资源实体 Map 集合
	 * 
	 * @return 合并好的树形资源实体 Map 集合
	 */
	public List<Map<String, Object>> mergeResources(List<Map<String, Object>> resources) {
		return mergeMenus(resources, null);
	}

	/**
	 * 合并资源，要获取资源的父类通过 "parent" key 来获取，如果不存在 "parent" key 表示该资源 Map 为根节点，
	 * 要获取资源的子类通过 "children" key 来获取
	 * 
	 * @param resources
	 *            要合并的资源实体 Map 集合
	 * @param ignoreType
	 *            要忽略资源类型
	 * 
	 * @return 合并好的树形资源实体 Map 集合
	 */
	public List<Map<String, Object>> mergeMenus(List<Map<String, Object>> resources, ResourceType ignoreType) {

		List<Map<String, Object>> result = Lists.newArrayList();
		for (Map<String, Object> entity : resources) {

			Long parentId = VariableUtils.typeCast(entity.get("parent_id"));
			Integer type = VariableUtils.typeCast(entity.get("type"));

			if (parentId == null && (ignoreType == null || !ignoreType.getValue().equals(type))) {
				recursionMenu(entity, resources, ignoreType);
				result.add(entity);
			}

		}

		return result;
	}

	/**
	 * 递归并合并资源到指定的父类
	 * 
	 * @param parent
	 *            父类
	 * @param resources
	 *            资源实体 Map 集合
	 * @param ignoreType
	 *            要忽略不合并的资源类型
	 */
	private void recursionMenu(Map<String, Object> parent, List<Map<String, Object>> resources,
			ResourceType ignoreType) {

		parent.put("children", Lists.newArrayList());

		for (Map<String, Object> entity : resources) {

			String parentId = VariableUtils.typeCast(entity.get("parent_id"));
			if (StringUtils.isEmpty(parentId)) {
				continue;
			}
			Integer type = VariableUtils.typeCast(entity.get("type"));
			String id = VariableUtils.typeCast(parent.get("id"));

			if ((ignoreType == null || !ignoreType.getValue().equals(type)) && StringUtils.equals(parentId, id)) {
				recursionMenu(entity, resources, ignoreType);
				List<Map<String, Object>> children = VariableUtils.typeCast(parent.get("children"));
				entity.put("parent", parent);
				children.add(entity);
			}
		}
	}
}
