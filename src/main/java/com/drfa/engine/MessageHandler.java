package com.drfa.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sanjiv on 2/19/2015.
 */
public class MessageHandler {
    BreakReport report;
    MessageProcessor messageProcessor;

    public MessageHandler(BreakReport report, MessageProcessor messageProcessor){
        this.report = report;
        this.messageProcessor = messageProcessor;
    }

    public boolean handleMessage(String message){
        if ("Exit".equalsIgnoreCase(message)) {
            System.out.println("Exit message recieved.." + message);
            System.out.println(report);
            return false;
        }else if(message.startsWith("SUMMARY:")){
            String threadName = message.substring(message.indexOf(":")+1, message.lastIndexOf(":"));
            if("BASE".equalsIgnoreCase(threadName)){
                String numberOfRecords = message.substring(message.lastIndexOf(":")+1, message.length());
                report.setBaseTotalRecords(new Integer(numberOfRecords));
            }else if("TARGET".equalsIgnoreCase(threadName)){
                String numberOfRecords = message.substring(message.lastIndexOf(":")+1, message.length());
                report.setTargetTotalRecords(new Integer(numberOfRecords));
            }
            return true;
        } else{
//            System.out.println(String.format("Take the message %s from the queue", message));
            messageProcessor.processMessage(message);
            return true;
        }
    }
    public void enrichBreakReportWithOneSidedBreak(final Map<String, String> storageMap){
        int baseOneSidedBreak = 0;
        int targetOneSidedBreak = 0;
        for(String key: storageMap.keySet()) {
            if(key.startsWith("BASE")){
                baseOneSidedBreak++;
            }else if(key.startsWith("TARGET")){
                targetOneSidedBreak++;
            }
        }
        report.setBaseOneSidedBreaks(baseOneSidedBreak);
        report.setTargetOneSidedBreaks(targetOneSidedBreak);
    }

    public void enrichBreakReportWithColumnDetails(){
        Map<Integer, Map<String, List<String>>> mapOfBreaks = messageProcessor.getMapOfBreaks();
        Map<String, List<Integer>> mapOfColumnBreaks = new HashMap<String, List<Integer>>();
        for(Integer i : mapOfBreaks.keySet()){
            Map<String, List<String>> columnBreakes = mapOfBreaks.get(i);
            for(String columnName: columnBreakes.keySet()){
                List<String> values = columnBreakes.get(columnName);
                List<Integer> listNumberOfBreaks = mapOfColumnBreaks.get(columnName);
                if(values.get(2).equalsIgnoreCase("NOT MATCHED")){
                    if(listNumberOfBreaks == null) {
                        listNumberOfBreaks = new ArrayList<Integer>();
                        listNumberOfBreaks.add(0,new Integer(1));
                        listNumberOfBreaks.add(1,new Integer(0));
                    }else{
                        int existingValue = listNumberOfBreaks.get(0) +1;
                        listNumberOfBreaks.remove(0);
                        listNumberOfBreaks.add(0, existingValue);
                    }
                }else if(values.get(2).equalsIgnoreCase("MATCHED")){
                    if(listNumberOfBreaks == null) {
                        listNumberOfBreaks = new ArrayList<Integer>();
                        listNumberOfBreaks.add(0,new Integer(0));
                        listNumberOfBreaks.add(1,new Integer(1));
                    }else{
                        int existingValue = listNumberOfBreaks.get(1)+1;
                        listNumberOfBreaks.remove(1);
                        listNumberOfBreaks.add(1, existingValue);
                    }
                }
                mapOfColumnBreaks.put(columnName,listNumberOfBreaks);
            }
        }
        report.setColumnBreaksCount(mapOfColumnBreaks);
    }

}
