package com.saituo.order.entity.user;

import com.saituo.order.entity.order.Product;

public class ProductOrder {

	// 产品订单编码
	private Integer registerNumber;
	// 客户订单编码
	private Long userOrderId;
	// 受理地市
	private Integer areaId;
	// 客户编码
	private Integer userId;
	// 产品编码
	private Long productId;
	// 组id
	private Integer groupId;
	// 品牌
	private Integer brandId;
	// 货号
	private String productNum;
	// 产品名称
	private String productName;
	// 目录价
	private Double orderFee;
	// 订购数量
	private Long orderNum;
	// 审批状态:0未处理 1.待审批;2.已驳回;3.审批通过;
	private String auditCd;
	// 状态:0未处理;1.已出单；2.已收货；3.已结款；-1.已取消
	private String statusCd;
	// 状态:0.初始 1.未开具发票 2.已开具发票3.已送发票
	private String invoiceStatus;
	// 创建时间
	private String acceptDate;
	// 最后修改时间
	private String changeDate;
	// 客户收货时间
	private String deliveryDate;
	// 产品项
	private Product product;
	// 实收价
	private Double pricePaidFee;
	// 使用积分
	private Double pointBalanceFee;
	// 总体价格＝单价＊个数
	private Double totalPrice;
	// 投诉状态
	private Integer complainCd;
	// 导师下单时间
	private String teacherOrderTime;

	public String getDeliveryDate() {
		return deliveryDate;
	}
	public void setDeliveryDate(String deliveryDate) {
		this.deliveryDate = deliveryDate;
	}
	public Double getPricePaidFee() {
		return pricePaidFee;
	}
	public void setPricePaidFee(Double pricePaidFee) {
		this.pricePaidFee = pricePaidFee;
	}

	public Double getPointBalanceFee() {
		return pointBalanceFee;
	}
	public void setPointBalanceFee(Double pointBalanceFee) {
		this.pointBalanceFee = pointBalanceFee;
	}
	public Integer getRegisterNumber() {
		return registerNumber;
	}
	public void setRegisterNumber(Integer registerNumber) {
		this.registerNumber = registerNumber;
	}
	public Long getUserOrderId() {
		return userOrderId;
	}
	public void setUserOrderId(Long userOrderId) {
		this.userOrderId = userOrderId;
	}
	public Integer getAreaId() {
		return areaId;
	}
	public void setAreaId(Integer areaId) {
		this.areaId = areaId;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	public Double getOrderFee() {
		return orderFee;
	}
	public void setOrderFee(Double orderFee) {
		this.orderFee = orderFee;
	}
	public Long getOrderNum() {
		return orderNum;
	}
	public void setOrderNum(Long orderNum) {
		this.orderNum = orderNum;
	}
	public String getAuditCd() {
		return auditCd;
	}
	public void setAuditCd(String auditCd) {
		this.auditCd = auditCd;
	}
	public String getStatusCd() {
		return statusCd;
	}
	public void setStatusCd(String statusCd) {
		this.statusCd = statusCd;
	}
	public String getInvoiceStatus() {
		return invoiceStatus;
	}
	public void setInvoiceStatus(String invoiceStatus) {
		this.invoiceStatus = invoiceStatus;
	}
	public String getAcceptDate() {
		return acceptDate;
	}
	public void setAcceptDate(String acceptDate) {
		this.acceptDate = acceptDate;
	}
	public String getChangeDate() {
		return changeDate;
	}
	public void setChangeDate(String changeDate) {
		this.changeDate = changeDate;
	}
	public Product getProduct() {
		return product;
	}
	public void setProduct(Product product) {
		this.product = product;
	}
	public Double getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(Double totalPrice) {
		this.totalPrice = totalPrice;
	}
	public Integer getComplainCd() {
		return complainCd;
	}
	public void setComplainCd(Integer complainCd) {
		this.complainCd = complainCd;
	}
	public Integer getBrandId() {
		return brandId;
	}
	public void setBrandId(Integer brandId) {
		this.brandId = brandId;
	}
	public String getProductNum() {
		return productNum;
	}
	public void setProductNum(String productNum) {
		this.productNum = productNum;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public Integer getGroupId() {
		return groupId;
	}
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
	public String getTeacherOrderTime() {
		return teacherOrderTime;
	}
	public void setTeacherOrderTime(String teacherOrderTime) {
		this.teacherOrderTime = teacherOrderTime;
	}

}
