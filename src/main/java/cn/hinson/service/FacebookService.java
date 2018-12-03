package cn.hinson.service;

import cn.hinson.model.FbGroup;
import cn.hinson.model.FbPage;
import com.alibaba.fastjson.JSONArray;
import com.restfb.*;
import com.restfb.json.JsonObject;
import com.restfb.types.GraphResponse;
import com.restfb.types.ResumableUploadStartResponse;
import com.restfb.types.ResumableUploadTransferResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @Created by shuxinsheng
 * @Date on 2018/12/3
 * @Description
 */
public class FacebookService {

    private static Version fbVersion = Version.VERSION_3_2;
    private static String proxyHost = "127.0.0.1";
    private static int proxyPort = 1080;


    /**
     * 获取用户的授权主页信息
     *
     * 访问 facebook 的 /me/accounts 获取用户授权的所有主页信息
     * @param accessToken
     * @return
     */
    public List<FbPage> getFBPages(String accessToken) {
        List<FbPage> fbPageList = null;
        FacebookClient facebookClient = null;
        try {
            facebookClient = this.getFacebookClient(accessToken);
            JsonObject resJson = facebookClient.fetchObject("me/accounts", JsonObject.class);
            JSONArray fbPageArray = JSONArray.parseArray(resJson.get("data").toString());
            fbPageList = fbPageArray.toJavaList(FbPage.class);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回 null，表示accessToken无效
            fbPageList = null;
        }
        return fbPageList;

    }

    /**
     * 获取用户授权的小组信息
     *
     * 先访问 /me 获取 id（user-id)
     * 然后访问 /user-id/groups 获取用户授权的所有小组信息
     * @param accessToken 用户的accessToken
     * @return null 代表accessToken无效；size()==0代表用户没有授权的小组； size()>0 返回了用户的小组信息List
     */
    public List<FbGroup> getFBGroups(String accessToken) {
        List<FbGroup> fbGroupList = null;
        FacebookClient facebookClient = null;
        try {
            facebookClient = this.getFacebookClient(accessToken);
            JsonObject resJson = facebookClient.fetchObject("me", JsonObject.class);
            String id = resJson.getString("id", null);
            JsonObject resJson1 = facebookClient.fetchObject(id + "/groups", JsonObject.class);
            JSONArray fbGroupArray = JSONArray.parseArray(resJson1.get("data").toString());
            fbGroupList = fbGroupArray.toJavaList(FbGroup.class);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回 null，表示accessToken无效
            fbGroupList = null;
        }
        return fbGroupList;
    }

    /**
     * 发帖，发纯文字贴，即使文字中带链接也不会生成预* @param id
     * @param msg
     * @return
     */
    private boolean publishMsg(String accessToken, String id, String msg) {
        String path = id + "/feed";
        Parameter[] parameters = new Parameter[1];
        parameters[0] = Parameter.with("message", msg);
        return this.publishBase(accessToken, path,null, parameters);
    }

    /**
     * 小组发帖，发纯文字帖，即使文字中带链接也不会生成预览
     *
     * 即使发帖成功，也有可能facebook审核不过
     * @param accessToken
     * @param groupId
     * @param msg
     * @return
     */
    public boolean publishMsgToGroup(String accessToken, String groupId, String msg) {
        return this.publishMsg(accessToken, groupId, msg);
    }

    /**
     * 主页发帖，发纯文字贴，即使文字中带链接也不会生成预览
     *
     * 同上小组发帖，只是groupId和pageId不同
     *
     * @param pageAccessToken
     * @param pageId
     * @param msg
     * @return
     */
    public boolean publishMsgToPage(String pageAccessToken, String pageId, String msg) {
        return this.publishMsg(pageAccessToken, pageId, msg);
    }

    /**
     * 发帖，发纯文字帖，带链接，会生成预览
     * @param accessToken
     * @param id
     * @param msg
     * @param link
     * @return
     */
    private boolean publishMsg(String accessToken, String id, String msg, String link) {
        String path = id + "/feed";
        Parameter[] parameters = new Parameter[2];
        parameters[0] = Parameter.with("message", msg);
        parameters[1] = Parameter.with("link", link);
        return this.publishBase(accessToken, path,null,  parameters);
    }

    /**
     * 小组发帖，发纯文字帖，带链接（链接会生成预览图）
     * @param accessToken
     * @param groupId
     * @param msg
     * @param link 想要生成预览的链接
     * @return
     */
    public boolean publishMsgToGroup(String accessToken, String groupId, String msg, String link) {
        return this.publishMsg(accessToken, groupId, msg, link);
    }

    /**
     * 主页发帖，发纯文字贴，带链接（链接会生成预览图）
     *
     * 同上小组发帖，只是groupId和pageId不同
     *
     * @param pageAccessToken
     * @param pageId
     * @param msg
     * @param link
     * @return
     */
    public boolean publishMsgToPage(String pageAccessToken, String pageId, String msg, String link) {
        return this.publishMsg(pageAccessToken, pageId, msg, link);
    }


    /**
     * 上传图片至 facebook 上，非本地文件上传，网络url上传
     * @param accessToken
     * @param id
     * @param caption
     * @param imageUrl
     * @return
     */
    private boolean publishPhotoUrl(String accessToken, String id, String caption, String imageUrl) {
        String path = id + "/photos";
        Parameter[] parameters = new Parameter[2];
        parameters[0] = Parameter.with("caption", caption);
        parameters[1] = Parameter.with("url", imageUrl);
        return this.publishBase(accessToken, path,null,  parameters);
    }

    /**
     * 小组上传，上传图片到 facebook 上，非本地文件上传，网络url上传
     * @param accessToken
     * @param groupId
     * @param caption
     * @param imageUrl
     * @return
     */
    public boolean publishPhotoUrlToGroup(String accessToken, String groupId, String caption, String imageUrl) {
        return this.publishPhotoUrl(accessToken, groupId, caption, imageUrl);
    }


    /**
     * 主页发帖，上传图片到 facebook 上，非本地文件上传，网络url上传
     *
     * 同上小组发帖，只是groupId和pageId不同
     *
     * @param pageAccessToken
     * @param pageId
     * @param caption
     * @param imageUrl
     * @return
     */
    public boolean publishPhotoUrlToPage(String pageAccessToken, String pageId, String caption, String imageUrl) {
        return this.publishPhotoUrl(pageAccessToken, pageId, caption, imageUrl);
    }

    /**
     * 上传图片到 facebook 上，本地文件上传
     * @param accessToken
     * @param id
     * @param caption
     * @param imagePath
     * @return
     */
    private boolean publishPhotoPath(String accessToken, String id, String caption, String imagePath) {
        String path = id + "/photos";

        List<BinaryAttachment> binaryAttachments = new ArrayList<>(1);

        InputStream inputStream = null;
        File file = new File(imagePath);
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        binaryAttachments.add(BinaryAttachment.with(file.getName(), inputStream));

        Parameter[] parameters = new Parameter[1];
        parameters[0] = Parameter.with("caption", caption);

        return publishBase(accessToken, path, binaryAttachments, parameters);
    }

    /**
     * 小组上传，上传图片到 facebook 上，本地文件上传
     * @param accessToken
     * @param groupId 组号
     * @param caption 描述
     * @param imagePath 本地路径
     * @return
     */
    public boolean publishPhotoPathToGroup(String accessToken, String groupId, String caption, String imagePath) {
        return this.publishPhotoPath(accessToken, groupId, caption, imagePath);
    }


    /**
     * 主页发帖，上传图片到 facebook 上，本地文件上传
     *
     * 同上小组发帖，只是groupId和pageId不同
     *
     * @param pageAccessToken
     * @param pageId
     * @param caption
     * @param imagePath
     * @return
     */
    public boolean publishPhotoPathToPage(String pageAccessToken, String pageId, String caption, String imagePath) {
        return this.publishPhotoPath(pageAccessToken, pageId, caption, imagePath);
    }


    /**
     * 小组上传，上传视频至 facebook 上,网络url方式，非本地
     * 请求facebook返回结果比较快，但实际上传facebook成功有一定延迟
     * @param accessToken
     * @param id
     * @param caption
     * @param videoUrl
     * @return
     */
    private boolean publishVideoUrl(String accessToken, String id, String caption, String videoUrl) {
        String path = id + "/videos";
        Parameter[] parameters = new Parameter[2];
        parameters[0] = Parameter.with("description", caption);
        parameters[1] = Parameter.with("file_url", videoUrl);
        return publishBase(accessToken, path, null, parameters);
    }

    /**
     * 小组上传，上传视频至 facebook 上,网络url方式，非本地
     * 请求facebook返回结果比较快，但实际上传facebook成功有一定延迟
     *
     * facebook文档：https://developers.facebook.com/docs/graph-api/video-uploads
     * 最大 1GB 和最长 20 分钟的视频，视频应在 5 分钟内下载
     * @param accessToken
     * @param groupId
     * @param caption
     * @param videoUrl
     * @return
     */
    public boolean publishVideoUrlToGroup(String accessToken, String groupId, String caption, String videoUrl) {
        return this.publishVideoUrl(accessToken, groupId, caption, videoUrl);
    }


    /**
     * 主页发帖，上传视频至 facebook 上，网络url方式，非本地
     * 请求facebook返回结果比较快，但实际上传facebook成功有一定延迟
     *
     * 同上小组发帖，只是groupId和pageId不同
     *
     * @param pageAccessToken
     * @param pageId
     * @param caption
     * @param videoUrl
     * @return
     */
    public boolean publishVideoUrlToPage(String pageAccessToken, String pageId, String caption, String videoUrl) {
        return this.publishVideoUrl(pageAccessToken, pageId, caption, videoUrl);
    }


    /**
     * 小组上传，上传视频至 facebook 上，本地path方式，本地上传
     * facebook文档：https://developers.facebook.com/docs/graph-api/video-uploads
     * 限制：支持上传最大 10GB 和最长 4 小时的视频
     *
     * 长视频响应较慢
     *
     * @param accessToken
     * @param id
     * @param caption
     * @param videoPath
     * @return
     */
    private boolean publishVideoPath(String accessToken, String id, String caption, String videoPath) {
        String path = id + "/videos";

        File videoFile = new File(videoPath);
        long filesizeInBytes = videoFile.length();
        try {
            // 第一阶段：开始上传
            FileInputStream in = new FileInputStream(videoFile);
            FacebookClient facebookClient = this.getFacebookClient(accessToken);
            ResumableUploadStartResponse returnValue = facebookClient.publish(path,
                    ResumableUploadStartResponse.class,
                    Parameter.with("upload_phase", "start"),
                    Parameter.with("file_size", filesizeInBytes));

            long startOffset = returnValue.getStartOffset();
            long endOffset = returnValue.getEndOffset();
            Long length = endOffset - startOffset;
            String uploadSessionId = returnValue.getUploadSessionId();

            // 第二阶段：分片上传
            // We have to upload the chunks in a loop
            while (length > 0) {
                // First copy bytes in byte array
                byte fileBytes[] = new byte[length.intValue()];
                try {
                    in.read(fileBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                    // 这里如果出现ioExcption，可以考虑重传，目前是放弃本地上传，直接返回false
                    return false;
                }

                // Make the request to Facebook
                ResumableUploadTransferResponse filePart = facebookClient.publish(path,
                        // The returned object
                        ResumableUploadTransferResponse.class,
                        // The file chunk that should be uploaded now
                        BinaryAttachment.with("video_file_chunk", fileBytes),
                        // Tell Facebook that we are in the transfer phase now
                        Parameter.with("upload_phase", "transfer"),
                        // The offset the file chunk starts
                        Parameter.with("start_offset", startOffset),
                        // The important session ID of this file transfer
                        Parameter.with("upload_session_id", uploadSessionId));

                // After uploading the chunk we recalculate the offsets according to the
                // information provided by Facebook
                startOffset = filePart.getStartOffset();
                endOffset = filePart.getEndOffset();
                length = endOffset - startOffset;
            }

            // 第三阶段：上传完成，发布
            GraphResponse finishResponse = facebookClient.publish(path, GraphResponse.class,
                    Parameter.with("upload_phase", "finish"),
                    Parameter.with("upload_session_id", uploadSessionId),
                    Parameter.with("description", caption));

            // 结束，返回
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 小组上传，上传视频至 facebook 上，本地path方式，本地上传
     * facebook文档：https://developers.facebook.com/docs/graph-api/video-uploads
     * 限制：支持上传最大 10GB 和最长 4 小时的视频
     * @param accessToken
     * @param groupId
     * @param caption 发文时候的文字描述
     * @param videoPath
     * @return
     */
    public boolean publishVideoPathToGroup(String accessToken, String groupId, String caption, String videoPath) {
        return this.publishVideoPath(accessToken, groupId, caption, videoPath);
    }


    /**
     * 小组上传，上传视频至 facebook 上，本地path方式，本地上传
     * facebook文档：https://developers.facebook.com/docs/graph-api/video-uploads
     * 限制：支持上传最大 10GB 和最长 4 小时的视频
     *
     * 同上小组发帖，只是groupId和pageId不同
     *
     * @param pageAccessToken
     * @param pageId
     * @param description
     * @param videoPath
     * @return
     */
    public boolean publishVideoPathToPage(String pageAccessToken, String pageId, String description, String videoPath) {
        return this.publishVideoPath(pageAccessToken, pageId, description, videoPath);
    }


    /**
     * 发布基本方法，发布到主页和小组都是path不同，parameter不同，封装到同一个方法中
     * @param accessToken
     * @param path
     * @param parameters
     * @return
     */
    private boolean publishBase(String accessToken, String path, List<BinaryAttachment> binaryAttachments, Parameter[] parameters) {
        FacebookClient facebookClient = this.getFacebookClient(accessToken);
        GraphResponse publishMessageResponse;
        if (binaryAttachments == null || binaryAttachments.size() == 0) {
            // 非二进制上传, 网络url上传
            publishMessageResponse =
                    facebookClient.publish(path, GraphResponse.class,
                            parameters);
        } else {
            // 二进制上传， 本地path上传
            publishMessageResponse =
                    facebookClient.publish(path, GraphResponse.class,
                            binaryAttachments,
                            parameters);
        }

        return publishMessageResponse.isSuccess();
    }

    /**
     * 根据accessToken生成FacebookClient，将该方法封装，方便其他方法使用，避免重复代
     * @param accessToken
     * @return
     */
    private FacebookClient getFacebookClient(String accessToken) {
        FacebookClient facebookClient = new DefaultFacebookClient(accessToken,
                this.getWebRequestor(), new DefaultJsonMapper(), fbVersion);
        return facebookClient;
    }
    /**
     *  设置facebook sdk restFB的代理, 供getFacebookClient使用
     * @return
     */
    private WebRequestor getWebRequestor() {

        return new DefaultWebRequestor(){
            // 重写 openConnection 方法，注入代理配置
            @Override
            protected HttpURLConnection openConnection(URL url) throws IOException {
                InetSocketAddress proxyLocation = new InetSocketAddress(proxyHost, proxyPort);
                Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyLocation);
                return (HttpURLConnection)url.openConnection(proxy);
            }
        };
    }
}
