package com.drfa.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;

/**
 * Created by Sanjiv on 2/12/2015.
 */

public class ScanFile {

    volatile int sharedVariable = 0;

    public void scanFile(int primaryKeyIndex, Map<String, String> storageMap, File fileToBeScanned,
                         BlockingQueue queue, String threadName) throws FileNotFoundException, InterruptedException {
        Scanner scanner = new Scanner(fileToBeScanned);
        int totalNumberOfRecords = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            ///System.out.println(String.format("This line is for thread %s with content %s",threadName,line));
            String splitLine[] = line.split(Pattern.quote("|"));
            String doesKeyExist = storageMap.get(checkPrefixOfTheKey(threadName)+splitLine[primaryKeyIndex]);
            if (doesKeyExist == null) {
                storageMap.put(threadName+":" + splitLine[primaryKeyIndex], line);
            } else {
                String stringToCompare = threadName + ":" + line + "$" + doesKeyExist;
                queue.put(stringToCompare);
                storageMap.remove(checkPrefixOfTheKey(threadName)+ splitLine[primaryKeyIndex]);
            }
            totalNumberOfRecords++;
        }
        sharedVariable++;
        if (sharedVariable == 2) {
            System.out.println("Publishing the exit message....");
            flushTheStorageMap(storageMap,queue);
            queue.put("SUMMARY:" + threadName+":"+totalNumberOfRecords);
            queue.put("Exit");
        } else {
            System.out.println(String.format("Ending of the thread %s with shared Counter %s", threadName, sharedVariable));
            queue.put("SUMMARY:" + threadName+":"+totalNumberOfRecords);
        }
        System.out.println(String.format("Size of the file hash map storage for thread: %s  is %s", threadName, storageMap.size()));
    }

    public String checkPrefixOfTheKey(String threadName){
        return "BASE".equalsIgnoreCase(threadName) ? "TARGET:" : "BASE:";
    }
    public void flushTheStorageMap(Map<String, String> storageMap, BlockingQueue queue) throws InterruptedException {
        Map<String, String> temporaryMap = new HashMap<String, String>();
        temporaryMap.putAll(storageMap);
        for(String key: temporaryMap.keySet()){
            if(key.startsWith("BASE")){
                String subStringKey = key.substring(key.indexOf(":")+1);
                String columnKey = "TARGET:" + subStringKey;
                if(storageMap.containsKey(columnKey)){
                    String message = "BASE:" + temporaryMap.get(key) + "$" + temporaryMap.get(columnKey);
                    queue.put(message);
                    storageMap.remove(columnKey);
                    storageMap.remove(key);
                }
            }else if(key.startsWith("TARGET")){
                String subStringKey = key.substring(key.indexOf(":")+1);
                String columnKey = "BASE:" + subStringKey;
                if(storageMap.containsKey(columnKey)){
                    String message = "BASE:" + temporaryMap.get(columnKey) + "$" + temporaryMap.get(key);
                    queue.put(message);
                    storageMap.remove(columnKey);
                    storageMap.remove(key);
                }
            }
        }
    }
}
