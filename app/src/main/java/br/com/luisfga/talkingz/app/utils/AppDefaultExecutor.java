package br.com.luisfga.talkingz.app.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class AppDefaultExecutor {

    //-----------------------------------------------------
    //------- THREAD PARA CARGA URGENTE - PRIORIDADE MÁXIMA (MainUser e Inicialização)
    //-----------------------------------------------------
    //thread com prioridade máxima para carregar itens obrigatórios fora da thread da interface
    private static ThreadFactory orchestraBackloadMaxPriorityThreadFactory = r -> {
        Thread orchestraBackLoadMaxPriorityThread = new Thread(r, "OrchestraBackLoadPriorityThread");
        orchestraBackLoadMaxPriorityThread.setPriority(Thread.MAX_PRIORITY);
        return orchestraBackLoadMaxPriorityThread;
    };
    private static ExecutorService orchestraBackloadMaxPriorityThread = Executors.newSingleThreadExecutor(orchestraBackloadMaxPriorityThreadFactory);
    public static ExecutorService getOrchestraBackloadMaxPriorityThread() {
        return orchestraBackloadMaxPriorityThread;
    }

    //-----------------------------------------------------
    //------- THREAD PARA CARGA IMPORTANTE - PRIORIDADE NORMAL (Conexão)
    //-----------------------------------------------------
    private static ExecutorService orchestraNormalPriorityThread = Executors.newSingleThreadExecutor();
    public static ExecutorService getOrchestraNormalPriorityThread() {
        return orchestraNormalPriorityThread;
    }

    //-----------------------------------------------------
    //------- THREAD PARA CARGA DE BAIXA PRIORIDADE
    //-----------------------------------------------------
    private static int countLowPriority = 1;
    private static ThreadFactory orchestraLowPriorityThreadFactory = r -> {
        Thread orchestraLowPriorityThread = new Thread(r, "orchestraLowPriorityThread"+ countLowPriority);
        countLowPriority++;
        orchestraLowPriorityThread.setPriority(Thread.MIN_PRIORITY);
        return orchestraLowPriorityThread;
    };
    private static ExecutorService orchestraLowPriorityThreadPool = Executors.newCachedThreadPool(orchestraLowPriorityThreadFactory);
    public static ExecutorService getOrchestraLowPriorityThreadPool() {
        return orchestraLowPriorityThreadPool;
    }

    //-----------------------------------------------------
    //------- THREAD LOW PRIORITY PARA NETWORKING
    //-----------------------------------------------------
    private static int countNetworking = 1;
    private static ThreadFactory orchestraLowPriorityNetworkingThreadFactory = r -> {
        Thread orchestraLowPriorityNetworkingThread = new Thread(r, "orchestraLowPriorityNetworkingThread"+ countNetworking);
        countNetworking++;
        orchestraLowPriorityNetworkingThread.setPriority(Thread.MIN_PRIORITY);
        return orchestraLowPriorityNetworkingThread;
    };
    private static ExecutorService orchestraLowPriorityNetworkingThreadPool = Executors.newCachedThreadPool(orchestraLowPriorityNetworkingThreadFactory);
    public static ExecutorService getOrchestraLowPriorityNetworkingThreadPool() {
        return orchestraLowPriorityNetworkingThreadPool;
    }
}