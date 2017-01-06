package com.taobao.rigel.rap.mock.web.action;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.taobao.rigel.rap.common.base.ActionBase;
import com.taobao.rigel.rap.common.utils.StringUtils;
import com.taobao.rigel.rap.common.utils.SystemVisitorLog;
import com.taobao.rigel.rap.mock.service.MockMgr;
import com.taobao.rigel.rap.project.bo.Action;
import com.taobao.rigel.rap.project.bo.Module;
import com.taobao.rigel.rap.project.bo.Page;
import com.taobao.rigel.rap.project.bo.Project;
import com.taobao.rigel.rap.project.service.ProjectMgr;
import org.apache.logging.log4j.LogManager;
import org.apache.struts2.ServletActionContext;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MockAction extends ActionBase {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getFormatterLogger(MockAction.class.getName());
    private int id;
    private int __id__;
    private String pattern;
    private String mockData;
    private int actionId;
    private int projectId;
    private String content;
    private String callback;
    private boolean enable = true;
    private String _c;
    private ProjectMgr projectMgr;
    private List<String> urlList;
    private boolean seajs;
    private boolean disableLog;
    private String mode;
    private MockMgr mockMgr;
    private String actionData;
    private String url;

    public MockAction() {
        System.out.println("MockAction");
    }

    public String getActionData() {
        return actionData;
    }

    public void setActionData(String actionData) {
        this.actionData = actionData;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isDisableLog() {
        return disableLog;
    }

    public void setDisableLog(boolean disableLog) {
        this.disableLog = disableLog;
    }

    public List<String> getUrlList() {
        return urlList;
    }

    public void setUrlList(List<String> urlList) {
        this.urlList = urlList;
    }

    private String getMethod() {
        return ServletActionContext.getRequest().getMethod();
    }

    public ProjectMgr getProjectMgr() {
        return projectMgr;
    }

    public void setProjectMgr(ProjectMgr projectMgr) {
        this.projectMgr = projectMgr;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isSeajs() {
        return seajs;
    }

    public void setSeajs(boolean seajs) {
        this.seajs = seajs;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setMockData(String mockData) {
        this.mockData = mockData;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    public String get_c() {
        return StringUtils.escapeInHJ(_c);
    }

    public void set_c(String _c) {
        this._c = _c;
    }

    public String getCallback() {
        return StringUtils.escapeInHJ(callback);
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public MockMgr getMockMgr() {
        return mockMgr;
    }

    public void setMockMgr(MockMgr mockMgr) {
        this.mockMgr = mockMgr;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPattern() {
        return pattern;
    }

    /**
     * force callback or _c to be the last parameter
     *
     * @param pattern
     */
    public void setPattern(String pattern) {
        if (this.pattern == null) {
            this.pattern = rewritePattern(pattern);
        }
    }

    public String createData() throws UnsupportedEncodingException {
        String callback = getCallback();
        boolean isJSON = false;
        updateProjectListMockNum(SystemVisitorLog.mock(__id__, "createData", pattern, getCurAccount()));
        Map<String, Object> options = new HashMap<String, Object>();
        String _c = get_c();
        String result = mockMgr.generateData(__id__, pattern, options);
        if (options.get("callback") != null) {
            _c = (String) options.get("callback");
            callback = (String) options.get("callback");
        }

        if (callback != null && !callback.isEmpty()) {
            setContent(callback + "(" + result + ")");
        } else if (_c != null && !_c.isEmpty()) {
            setContent(_c + "(" + result + ")");
        } else {
            isJSON = true;
            setContent(result);
        }
        if (isJSON) {
            return "json";
        } else {
            return SUCCESS;
        }
    }

    @SuppressWarnings("unchecked")
    private String rewritePattern(String pattern) {
        Set<String> blackProps = new HashSet<String>();
        blackProps.add("__id__");
        blackProps.add("callback");
        HttpServletRequest request = ServletActionContext.getRequest();
        String method = request.getMethod();
        if (!"POST".equals(method) && !"POST".equals("PUT")) {
            return pattern;
        }
        // rebuild pattern
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(pattern);
        List<MediaType> types = MediaType.parseMediaTypes(request.getHeader("Content-Type"));
        MediaType.sortBySpecificityAndQuality(types);
        for (MediaType type : types) {
            if (type.isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED)) {
                // POST
                Enumeration<String> names = (Enumeration<String>) request.getParameterNames();
                while (names.hasMoreElements()) {
                    String name = names.nextElement();
                    if (blackProps.contains(name)) {
                        continue;
                    }
                    List<String> vals = new ArrayList<String>();
                    vals.addAll(Arrays.asList(request.getParameterValues(name)));
                    if ("pattern".equals(name)) {
                        vals.remove(pattern);
                        if (vals.isEmpty()) {
                            continue;
                        }
                    }
                    builder.replaceQueryParam(name, (Object[]) vals.toArray());
                }
                return builder.build().toString();
            }
            if (type.isCompatibleWith(MediaType.APPLICATION_JSON)) {
                try {
                    String content = StreamUtils.copyToString(request.getInputStream(), Charset.forName("utf-8"));
                    Gson gson = new GsonBuilder().create();
                    Map<String, Object> map = gson.fromJson(content, Map.class);
                    Map<String, String> params = resolveMap(map);
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        builder.replaceQueryParam(entry.getKey(), entry.getValue());
                    }
                    return builder.build().toString();
                } catch (IOException ex) {
                    break;
                }
            }
        }
        return pattern;
    }

    private Map<String, String> resolveMap(Map<String, Object> map) {
        Map<String, String> params = new HashMap<String, String>();
        resolveObject(map, "", params);
        return params;
    }

    @SuppressWarnings("unchecked")
    private void resolveObject(Object val, String prefix, Map<String, String> output) {
        if (val instanceof Map) {
            resolveMap((Map<String, Object>) val, prefix, output);
        } else if (val instanceof List) {
            resolveList((List<Object>) val, prefix, output);
        } else {
            if (val != null) {
                output.put(prefix, val.toString());
            } else {
                output.put(prefix, null);
            }
        }
    }

    private void resolveList(List<Object> list, String prefix, Map<String, String> output) {
        for (int i = 0; i < list.size(); i++) {
            String pref = prefix + "[" + i + "]";
            Object val = list.get(i);
            resolveObject(val, pref, output);
        }
    }


    @SuppressWarnings("unchecked")
    private void resolveMap(Map<String, Object> map, String prefix, Map<String, String> output) {
        String newPrefix = prefix;
        if (!org.springframework.util.StringUtils.isEmpty(newPrefix)) {
            newPrefix += ".";
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String name = entry.getKey();
            String pref = newPrefix + name;
            Object val = entry.getValue();
            resolveObject(val, pref, output);
        }
    }

    public String createRule() throws UnsupportedEncodingException {
        String callback = getCallback();
        boolean isJSON = false;
        updateProjectListMockNum(SystemVisitorLog.mock(__id__, "createRule", pattern, getCurAccount()));
        Map<String, Object> options = new HashMap<String, Object>();
        String _c = get_c();
        options.put("method", getMethod());
        String result = mockMgr.generateRule(__id__, pattern, options);
        if (options.get("callback") != null) {
            _c = (String) options.get("callback");
            callback = (String) options.get("callback");
        }
        if (callback != null && !callback.isEmpty()) {
            setContent(callback + "(" + result + ")");
        } else if (_c != null && !_c.isEmpty()) {
            setContent(_c + "(" + result + ")");
        } else {
            isJSON = true;
            setContent(result);
        }
        if (isJSON) {
            return "json";
        } else {
            return SUCCESS;
        }
    }

    public String createRuleAuto() throws UnsupportedEncodingException {
        String callback = getCallback();
        boolean isJSON = false;
        updateProjectListMockNum(SystemVisitorLog.mock(__id__, "createRule", pattern, getCurAccount()));
        Map<String, Object> options = new HashMap<String, Object>();
        String _c = get_c();
        options.put("method", getMethod());
        options.put("loadRule", true); // load rules set by Open API (tb_rule)

        String result = mockMgr.generateRule(__id__, pattern, options);
        if (options.get("callback") != null) {
            _c = (String) options.get("callback");
            callback = (String) options.get("callback");
        }
        if (callback != null && !callback.isEmpty()) {
            setContent(callback + "(" + result + ")");
        } else if (_c != null && !_c.isEmpty()) {
            setContent(_c + "(" + result + ")");
        } else {
            isJSON = true;
            setContent(result);
        }
        if (isJSON) {
            return "json";
        } else {
            return SUCCESS;
        }
    }

    public String createRuleByActionData() throws UnsupportedEncodingException {
        String callback = getCallback();
        boolean isJSON = false;
        updateProjectListMockNum(SystemVisitorLog.mock(id, "createRuleByActionData", pattern, getCurAccount()));
        Map<String, Object> options = new HashMap<String, Object>();
        String _c = get_c();
        String result = mockMgr.generateRule(id, pattern, options);
        if (options.get("callback") != null) {
            _c = (String) options.get("callback");
            callback = (String) options.get("callback");
        }
        if (callback != null && !callback.isEmpty()) {
            setContent(callback + "(" + result + ")");
        } else if (_c != null && !_c.isEmpty()) {
            setContent(_c + "(" + result + ")");
        } else {
            isJSON = true;
            setContent(result);
        }
        if (isJSON) {
            return "json";
        } else {
            return SUCCESS;
        }
    }

    public String modify() {
        setNum(mockMgr.modify(actionId, mockData));
        return SUCCESS;
    }

    public String reset() {
        setNum(mockMgr.reset(projectId));
        return SUCCESS;
    }

    public String createPluginScript() {
        updateProjectListMockNum(SystemVisitorLog.mock(id, "createPluginScript", pattern, getCurAccount()));
        Map<String, Boolean> _circleRefProtector = new HashMap<String, Boolean>();
        List<String> list = new ArrayList<String>();
        Project p = projectMgr.getProject(projectId);

        loadWhiteList(p, list, _circleRefProtector);
        urlList = list;
        return SUCCESS;
    }

    public String getWhiteList() {
        Map<String, Boolean> _circleRefProtector = new HashMap<String, Boolean>();
        List<String> list = new ArrayList<String>();
        Project p = projectMgr.getProject(projectId);

        loadWhiteList(p, list, _circleRefProtector);
        urlList = list;
        Gson g = new Gson();
        String json = g.toJson(urlList);
        setJson(json);

        return SUCCESS;
    }

    private void loadWhiteList(Project p, List<String> list, Map<String, Boolean> map) {
        // prevent circle reference
        if (p == null || map.get(p.getId() + "") != null) {
            return;
        } else {
            map.put(p.getId() + "", true);
        }
        if (p != null) {
            for (Module m : p.getModuleList()) {
                for (Page page : m.getPageList()) {
                    for (Action a : page.getActionList()) {
                        list.add(a.getRequestUrlRel());
                    }
                }
            }
        }

        String relatedIds = p.getRelatedIds();
        if (relatedIds != null && !relatedIds.isEmpty()) {
            String[] relatedIdsArr = relatedIds.split(",");
            for (String relatedId : relatedIdsArr) {
                int rId = Integer.parseInt(relatedId);
                Project rP = projectMgr.getProject(rId);
                if (rP != null && rP.getId() > 0)
                    loadWhiteList(rP, list, map);
            }
        }
    }

    public String createMockjsData() throws UnsupportedEncodingException {
        String callback = getCallback();
        boolean isJSON = false;
        updateProjectListMockNum(SystemVisitorLog.mock(__id__, "createMockjsData", pattern, getCurAccount()));
        String _c = get_c();
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("method", getMethod());
        String result = mockMgr.generateRuleData(__id__, pattern, options);
        if (options.get("callback") != null) {
            _c = (String) options.get("callback");
            callback = (String) options.get("callback");
        }
        if (callback != null && !callback.isEmpty()) {
            setContent(callback + "(" + result + ")");
        } else if (_c != null && !_c.isEmpty()) {
            setContent(_c + "(" + result + ")");
        } else {
            isJSON = true;
            setContent(result);
        }

        if (isJSON) {
            return "json";
        } else {
            return SUCCESS;
        }
    }

    public String createMockjsDataAuto() throws UnsupportedEncodingException {
        String callback = getCallback();
        boolean isJSON = false;
        updateProjectListMockNum(SystemVisitorLog.mock(__id__, "createMockjsData", pattern, getCurAccount()));
        String _c = get_c();
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("method", getMethod());
        options.put("loadRule", true);
        String result = mockMgr.generateRuleData(__id__, pattern, options);
        if (options.get("callback") != null) {
            _c = (String) options.get("callback");
            callback = (String) options.get("callback");
        }
        if (callback != null && !callback.isEmpty()) {
            setContent(callback + "(" + result + ")");
        } else if (_c != null && !_c.isEmpty()) {
            setContent(_c + "(" + result + ")");
        } else {
            isJSON = true;
            setContent(result);
        }

        if (isJSON) {
            return "json";
        } else {
            return SUCCESS;
        }
    }

    public String validateAPI() throws UnsupportedEncodingException {
        String callback = getCallback();
        boolean isJSON = false;
        updateProjectListMockNum(SystemVisitorLog.mock(id, "createRule", pattern, getCurAccount()));
        Map<String, Object> options = new HashMap<String, Object>();
        String _c = get_c();
        options.put("method", getMethod());

        String result = mockMgr.validateAPI(get__id__(), pattern, options, getJson());
        if (options.get("callback") != null) {
            _c = (String) options.get("callback");
            callback = (String) options.get("callback");
        }
        if (callback != null && !callback.isEmpty()) {
            setContent(callback + "(" + result + ")");
        } else if (_c != null && !_c.isEmpty()) {
            setContent(_c + "(" + result + ")");
        } else {
            isJSON = true;
            setContent(result);
        }
        if (isJSON) {
            return "json";
        } else {
            return SUCCESS;
        }
    }

    private void updateProjectListMockNum(List<Project> list) {
        for (Project p : list) {
            Project project = projectMgr.getProject(p.getId());
            if (project == null) continue;
            project.setMockNum(p.getMockNum() + project.getMockNum());
            projectMgr.updateProjectNum(project);
        }
    }

    public int get__id__() {
        return __id__;
    }

    public void set__id__(int __id__) {
        this.__id__ = __id__;
    }

    public String requestOnServer() {
        setContent("因安全原因,暂时停止该接口.");
        return SUCCESS;

//        try {
//            setContent(HTTPUtils.sendGet(url));
//        } catch (Exception e) {
//            setContent(e.getMessage());
//        }
//
//        String contentLowerCase = this.content.toLowerCase();
//        if (contentLowerCase.contains("<!doctype") || contentLowerCase.contains("<html") || contentLowerCase.contains("<head") || contentLowerCase.contains("<body")) {
//            content = "请求不合法,因安全原因,暂时不支持对HTML页面的模拟. Illegal request, for security reason, temply deny all HTML responses.";
//        }
//
//        return SUCCESS;
    }

    public String queryMockData() {
        Gson gson = new Gson();
        Action action = gson.fromJson(actionData, Action.class);
        setContent(mockMgr.getMockRuleFromActionAndRule(null, action));

        return SUCCESS;
    }
}
