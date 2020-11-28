package br.com.luisfga.talkingz.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class AppDefaultExecutor {

    //-----------------------------------------------------
    //------- THREAD PARA CARGA URGENTE - PRIORIDADE MÁXIMA (MainUser e Inicialização)
    //-----------------------------------------------------
    //thread com prioridade máxima para carregar itens obrigatórios fora da thread da interface
    private static ThreadFactory talkingzBackloadMaxPriorityThreadFactory = r -> {
        Thread talkingzBackLoadMaxPriorityThread = new Thread(r, "talkingzBackLoadPriorityThread");
        talkingzBackLoadMaxPriorityThread.setPriority(Thread.MAX_PRIORITY);
        return talkingzBackLoadMaxPriorityThread;
    };
    private static ExecutorService talkingzBackloadMaxPriorityThread = Executors.newSingleThreadExecutor(talkingzBackloadMaxPriorityThreadFactory);
    public static ExecutorService getTalkingzBackloadMaxPriorityThread() {
        return talkingzBackloadMaxPriorityThread;
    }

    //-----------------------------------------------------
    //------- THREAD PARA CARGA IMPORTANTE - PRIORIDADE NORMAL (Conexão)
    //-----------------------------------------------------
    private static ExecutorService talkingzNormalPriorityThread = Executors.newSingleThreadExecutor();
    public static ExecutorService getTalkingzNormalPriorityThread() {
        return talkingzNormalPriorityThread;
    }

    //-----------------------------------------------------
    //------- THREAD PARA CARGA DE BAIXA PRIORIDADE
    //-----------------------------------------------------
    private static int countLowPriority = 1;
    private static ThreadFactory talkingzLowPriorityThreadFactory = r -> {
        Thread talkingzLowPriorityThread = new Thread(r, "talkingzLowPriorityThread"+ countLowPriority);
        countLowPriority++;
        talkingzLowPriorityThread.setPriority(Thread.MIN_PRIORITY);
        return talkingzLowPriorityThread;
    };
    private static ExecutorService talkingzLowPriorityThreadPool = Executors.newCachedThreadPool(talkingzLowPriorityThreadFactory);
    public static ExecutorService getTalkingzLowPriorityThreadPool() {
        return talkingzLowPriorityThreadPool;
    }

    //-----------------------------------------------------
    //------- THREAD LOW PRIORITY PARA NETWORKING
    //-----------------------------------------------------
    private static int countNetworking = 1;
    private static ThreadFactory talkingzLowPriorityNetworkingThreadFactory = r -> {
        Thread talkingzLowPriorityNetworkingThread = new Thread(r, "talkingzLowPriorityNetworkingThread"+ countNetworking);
        countNetworking++;
        talkingzLowPriorityNetworkingThread.setPriority(Thread.MIN_PRIORITY);
        return talkingzLowPriorityNetworkingThread;
    };
    private static ExecutorService talkingzLowPriorityNetworkingThreadPool = Executors.newCachedThreadPool(talkingzLowPriorityNetworkingThreadFactory);
    public static ExecutorService getTalkingzLowPriorityNetworkingThreadPool() {
        return talkingzLowPriorityNetworkingThreadPool;
    }
}