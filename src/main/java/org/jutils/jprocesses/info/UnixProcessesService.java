/*
 * Copyright 2016 Javier Garcia Alonso.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jutils.jprocesses.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jutils.jprocesses.model.JProcessesResponse;
import org.jutils.jprocesses.model.ProcessInfo;
import org.jutils.jprocesses.util.ProcessesUtils;

/**
 * Service implementation for Unix/Linux
 *
 * @author Javier Garcia Alonso
 */
class UnixProcessesService extends AbstractProcessesService {
    //ps -C apache2
    private static final String PS_COLUMNS = "pid,ruser,vsize,rssize,%cpu,bsdstart,bsdtime,nice,comm,cmd";

    
    protected List<Map<String, String>> parseList(String rawData) {
        List<Map<String, String>> processesDataList = new ArrayList<Map<String, String>>();
        String[] dataStringLines = rawData.split("\\r?\\n");

        for (final String dataLine : dataStringLines) {
            if (!(dataLine.trim().startsWith("PID"))) {
                Map<String, String> element = new HashMap<String, String>();
                String[] elements = dataLine.split("\\s+");
                if (elements.length > 10) {
                    element.put("pid", elements[1]);
                    element.put("user", elements[2]);
                    element.put("virtual_memory", elements[3]);
                    element.put("physical_memory", elements[4]);
                    element.put("cpu_usage", elements[5]);
                    element.put("start_time", elements[6]);
                    element.put("proc_time", elements[7]);
                    element.put("priority", elements[8]);
                    element.put("proc_name", elements[9]);
                    element.put("command", elements[10]);

                    processesDataList.add(element);
                }
            }
        }

        return processesDataList;
    }
    
    @Override
    protected String getProcessesData(String name) {
        if (name != null) {
            return ProcessesUtils.executeCommand("ps", 
                    "o", PS_COLUMNS, "-C", name);
        }
        return ProcessesUtils.executeCommand("ps", 
                "o", PS_COLUMNS, "-e");
    }

    @Override
    protected JProcessesResponse kill(int pid) {
        JProcessesResponse response = new JProcessesResponse();
        if (ProcessesUtils.executeCommandAndGetCode("kill", "-9", String.valueOf(pid)) == 0) {
            response.setSuccess(true);
        }
        return response;
    }

    public JProcessesResponse changePriority(int pid, int priority) {
        JProcessesResponse response = new JProcessesResponse();
        if (ProcessesUtils.executeCommandAndGetCode("renice", String.valueOf(priority), 
                "-p", String.valueOf(pid)) == 0) {
            response.setSuccess(true);
        }
        return response;
    }

    public ProcessInfo getProcess(int pid) {
        List<Map<String, String>> processList = 
                parseList(ProcessesUtils.executeCommand("ps", 
                "o", PS_COLUMNS, "-p", String.valueOf(pid)));
        
        if (processList != null && !processList.isEmpty()) {
            Map<String, String> processData = processList.get(0);
            ProcessInfo info = new ProcessInfo();
            info.setPid(processData.get("pid"));
            info.setName(processData.get("proc_name"));
            info.setTime(processData.get("proc_time"));
            info.setCommand(processData.get("command"));
            info.setCpuUsage(processData.get("cpu_usage"));
            info.setPhysicalMemory(processData.get("physical_memory"));
            info.setStartTime(processData.get("start_time"));
            info.setUser(processData.get("user"));
            info.setVirtualMemory(processData.get("virtual_memory"));
            info.setPriority(processData.get("priority"));
            
            return info;
        }
        return null;
    }
}
