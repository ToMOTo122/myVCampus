// 文件路径: src/main/java/com/vcampus/server/service/ShopService.java
//yhr9.14 9:43修改
package com.vcampus.server.service;

import com.vcampus.common.entity.Message;
import com.vcampus.common.entity.Message.Code;
import com.vcampus.common.entity.Message.Type;
import com.vcampus.common.entity.Product;
import com.vcampus.common.entity.ShoppingCartItem;
import com.vcampus.common.entity.User;
import com.vcampus.server.dao.ProductDao;
import com.vcampus.server.dao.ShoppingCartDao;

import java.util.List;

/**
 * 商店服务类
 * 提供商品展示、购买等功能
 */
public class ShopService {

    private ProductDao productDao = new ProductDao();
    private ShoppingCartDao shoppingCartDao = new ShoppingCartDao();

    /**
     * 处理商店相关请求
     */
    public Message handleRequest(Message message, User currentUser) {
        try {
            Type type = message.getType();

            switch (type) {
                case SHOP_LIST:
                    return handleGetProductList();
                case SEARCH_PRODUCT:
                    return handleSearchProduct(message);
                case ADD_TO_CART:
                    return handleAddToCart(message);
                case GET_CART_COUNT:
                    return handleGetCartCount(message);
                case GET_CART_ITEMS:
                    return handleGetCartItems(message);
                case UPDATE_CART_ITEM:
                    return handleUpdateCartItem(message);
                case REMOVE_CART_ITEM:
                    return handleRemoveCartItem(message);
                case CHECKOUT:
                    return handleCheckout(message);
                default:
                    // 如果请求类型没有被处理，返回错误信息
                    return Message.error(Code.ERROR, "不支持的操作类型：" + type);
            }

        } catch (Exception e) {
            System.err.println("处理商店请求失败: " + e.getMessage());
            e.printStackTrace();
            return Message.error(Code.ERROR, "服务器内部错误: " + e.getMessage());
        }
    }

    //-------------------- 商品展示相关 --------------------

    private Message handleGetProductList() {
        try {
            List<Product> products = productDao.getAllProducts();
            return Message.success(products);
        } catch (Exception e) {
            e.printStackTrace();
            return Message.error(Code.ERROR, "获取商品列表失败");
        }
    }

//    private Message handleSearchProduct(Message message) {
//        try {
//            String keyword = (String) message.getData();
//            // TODO: 实现搜索商品的数据库查询逻辑
//            // 你的 ProductDao 需要一个 searchProducts(String keyword) 方法
//            // List<Product> products = productDao.searchProducts(keyword);
//
//            // 临时伪代码，你需要替换为实际的数据库访问
//            List<Product> allProducts = productDao.getAllProducts();
//            List<Product> searchResults = allProducts.stream()
//                    .filter(p -> p.getProductName().toLowerCase().contains(keyword.toLowerCase()))
//                    .collect(Collectors.toList());
//
//            return Message.success(searchResults);
//        } catch (Exception e) {
//            System.err.println("搜索商品失败: " + e.getMessage());
//            e.printStackTrace();
//            return Message.error(Code.ERROR, "搜索商品失败");
//        }
//    }
    //yhr9.14 10：09修改上面的方法如下：
    private Message handleSearchProduct(Message message) {
        try {
            String keyword = (String) message.getData();
            // 直接调用 DAO 层的搜索方法
            List<Product> searchResults = productDao.searchProducts(keyword);
            return Message.success(searchResults);
        } catch (Exception e) {
            e.printStackTrace();
            return Message.error(Code.ERROR, "搜索商品失败");
        }
    }


    //-------------------- 购物车相关 --------------------

//    private Message handleAddToCart(Message message) {
//        try {
//            ShoppingCartItem item = (ShoppingCartItem) message.getData();
//            // TODO: 实现将商品添加到购物车的数据库逻辑
//            // 你的 ShoppingCartDao 需要一个 addOrUpdateItem(ShoppingCartItem item) 方法
//            // shoppingCartDao.addOrUpdateItem(item);
//
//            return Message.success("成功加入购物车");
//        } catch (Exception e) {
//            System.err.println("添加商品到购物车失败: " + e.getMessage());
//            e.printStackTrace();
//            return Message.error(Code.ERROR, "添加商品到购物车失败");
//        }
//    }
    //yhr9.14 10：10修改上面方法如下：
    private Message handleAddToCart(Message message) {
        try {
            ShoppingCartItem item = (ShoppingCartItem) message.getData();
            // 直接调用 DAO 层的方法
            boolean success = shoppingCartDao.addOrUpdateItem(item);
            if (success) {
                return Message.success("成功加入购物车");
            } else {
                return Message.error(Code.ERROR, "加入购物车失败：数据库操作失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Message.error(Code.ERROR, "添加商品到购物车失败");
        }
    }

//    private Message handleGetCartCount(Message message) {
//        try {
//            String userId = (String) message.getData();
//            // TODO: 实现获取购物车商品总数的数据库逻辑
//            // 你的 ShoppingCartDao 需要一个 getCartCount(String userId) 方法
//            // int count = shoppingCartDao.getCartCount(userId);
//
//            // 临时伪代码
//            int count = 1;
//
//            return Message.success(count);
//        } catch (Exception e) {
//            System.err.println("获取购物车数量失败: " + e.getMessage());
//            e.printStackTrace();
//            return Message.error(Code.ERROR, "获取购物车数量失败");
//        }
//    }
    //yhr9.14 10：11修改上面代码如下：
    private Message handleGetCartCount(Message message) {
        try {
            String userId = (String) message.getData();
            // 直接调用 DAO 层的方法
            int count = shoppingCartDao.getCartCount(userId);
            return Message.success(count);
        } catch (Exception e) {
            e.printStackTrace();
            return Message.error(Code.ERROR, "获取购物车数量失败");
        }
    }

//    private Message handleGetCartItems(Message message) {
//        try {
//            String userId = (String) message.getData();
//            // TODO: 实现获取购物车商品列表的数据库逻辑
//            // 你的 ShoppingCartDao 需要一个 getCartItems(String userId) 方法
//            // List<ShoppingCartItem> items = shoppingCartDao.getCartItems(userId);
//
//            // 临时伪代码
//            List<ShoppingCartItem> items = new ArrayList<>();
//            items.add(new ShoppingCartItem("2021001", "P001", "VCampus 卫衣", 1, new BigDecimal("128.00")));
//
//            return Message.success(items);
//        } catch (Exception e) {
//            System.err.println("获取购物车商品失败: " + e.getMessage());
//            e.printStackTrace();
//            return Message.error(Code.ERROR, "获取购物车商品失败");
//        }
//    }
    //yhr9.14 10：11修改上面代码如下：
    private Message handleGetCartItems(Message message) {
        try {
            String userId = (String) message.getData();
            // 直接调用 DAO 层的方法
            List<ShoppingCartItem> items = shoppingCartDao.getCartItems(userId);
            return Message.success(items);
        } catch (Exception e) {
            e.printStackTrace();
            return Message.error(Code.ERROR, "获取购物车商品失败");
        }
    }


//    private Message handleUpdateCartItem(Message message) {
//        try {
//            ShoppingCartItem item = (ShoppingCartItem) message.getData();
//            // TODO: 实现更新购物车商品数量的数据库逻辑
//            // 你的 ShoppingCartDao 需要一个 updateItemQuantity(ShoppingCartItem item) 方法
//            // shoppingCartDao.updateItemQuantity(item);
//
//            return Message.success("更新成功");
//        } catch (Exception e) {
//            System.err.println("更新购物车商品失败: " + e.getMessage());
//            e.printStackTrace();
//            return Message.error(Code.ERROR, "更新购物车商品失败");
//        }
//    }
    //yhr9.14 10：12修改上面代码如下：
    private Message handleUpdateCartItem(Message message) {
        try {
            ShoppingCartItem item = (ShoppingCartItem) message.getData();
            boolean success = shoppingCartDao.updateItemQuantity(item);
            if (success) {
                return Message.success("更新成功");
            } else {
                return Message.error(Code.ERROR, "更新数量失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Message.error(Code.ERROR, "更新购物车商品失败");
        }
    }

//    private Message handleRemoveCartItem(Message message) {
//        try {
//            ShoppingCartItem item = (ShoppingCartItem) message.getData();
//            // TODO: 实现删除购物车商品的数据库逻辑
//            // 你的 ShoppingCartDao 需要一个 removeItem(String userId, String productId) 方法
//            // shoppingCartDao.removeItem(item.getUserId(), item.getProductId());
//
//            return Message.success("删除成功");
//        } catch (Exception e) {
//            System.err.println("删除购物车商品失败: " + e.getMessage());
//            e.printStackTrace();
//            return Message.error(Code.ERROR, "删除购物车商品失败");
//        }
//    }
    //yhr9.14 10：12修改上面代码如下：
    private Message handleRemoveCartItem(Message message) {
        try {
            ShoppingCartItem item = (ShoppingCartItem) message.getData();
            boolean success = shoppingCartDao.removeItem(item.getUserId(), item.getProductId());
            if (success) {
                return Message.success("删除成功");
            } else {
                return Message.error(Code.ERROR, "删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Message.error(Code.ERROR, "删除购物车商品失败");
        }
    }

//    private Message handleCheckout(Message message) {
//        try {
//            String userId = (String) message.getData();
//            // TODO: 实现结账逻辑，包括：
//            // 1. 获取用户购物车商品列表
//            // 2. 检查库存
//            // 3. 扣除用户余额
//            // 4. 生成订单记录
//            // 5. 清空购物车
//
//            return Message.success("购买成功！");
//        } catch (Exception e) {
//            System.err.println("结账失败: " + e.getMessage());
//            e.printStackTrace();
//            return Message.error(Code.ERROR, "结账失败");
//        }
//    }
    //yhr9.14 10：13修改上面代码如下：
    private Message handleCheckout(Message message) {
        try {
            String userId = (String) message.getData();
            // TODO: 实现结账的完整业务逻辑
            return Message.success("购买成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return Message.error(Code.ERROR, "结账失败");
        }
    }
}