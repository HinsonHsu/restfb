package cn.hinson.model;


/**
 * @Created by shuxinsheng
 * @Date on 2018/11/29
 * @Description 用于保存请求facebook的返回信息，me/accounts接口返回的page信息
 * 这里的page accessToken 跟 user的accessToken 不一样
 */

public class FbPage {
    private String accessToken;
    private String category;
    private String name;
    private String id;
}
