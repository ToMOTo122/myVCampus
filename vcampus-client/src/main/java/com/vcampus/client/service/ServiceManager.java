package com.vcampus.client.service;

/**
 * 静态服务管理器 - 管理全局ClientService实例
 */
public class ServiceManager {
    private static ServiceManager instance;
    private ClientService clientService;

    private ServiceManager() {}

    public static ServiceManager getInstance() {
        if (instance == null) {
            synchronized (ServiceManager.class) {
                if (instance == null) {
                    instance = new ServiceManager();
                }
            }
        }
        return instance;
    }

    /**
     * 设置ClientService - 在登录成功后调用
     */
    public void setClientService(ClientService clientService) {
        System.out.println("=== ServiceManager.setClientService 被调用 ===");
        System.out.println("传入的 ClientService: " + (clientService != null ? "不为空" : "为空"));

        if (clientService != null) {
            System.out.println("ClientService 连接状态: " + clientService.isConnected());
            System.out.println("ClientService 服务器地址: " + clientService.getServerAddress());
        }

        // 打印调用栈，帮助诊断
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        System.out.println("调用栈：");
        for (int i = 1; i <= Math.min(4, stack.length - 1); i++) {
            System.out.println("  " + stack[i].getClassName() + "." + stack[i].getMethodName() + ":" + stack[i].getLineNumber());
        }

        this.clientService = clientService;
        System.out.println("ServiceManager: ClientService 设置完成");
    }

    /**
     * 获取ClientService
     */
    public ClientService getClientService() {
        if (clientService == null) {
            System.err.println("=== ServiceManager: ClientService 为 null！===");
            System.err.println("请确保已登录并正确调用 setClientService 方法");
        } else {
            System.out.println("ServiceManager: 成功返回 ClientService，连接状态: " + clientService.isConnected());
        }
        return clientService;
    }

    /**
     * 检查服务是否可用
     */
    public boolean isServiceAvailable() {
        boolean available = clientService != null && clientService.isConnected();
        System.out.println("ServiceManager: 服务可用状态 = " + available);
        return available;
    }

    /**
     * 清除服务 - 在登出时调用
     */
    public void clearService() {
        System.out.println("ServiceManager: 清除 ClientService");
        if (clientService != null) {
            clientService.disconnect();
        }
        this.clientService = null;
    }
}