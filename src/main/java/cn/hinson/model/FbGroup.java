package cn.hinson.model;


/**
 * @Created by shuxinsheng
 * @Date on 2018/11/29
 * @Description
 *   accessToken为用户的accessToken，同一个用户的所有小组的accessToken均为一个，且为用户的accessToken
 */

public class FbGroup {

    private String id;

    private String name;

    // accessToken为用户的accessToken，同一个用户的所有小组的accessToken均为一个，且为用户的accessToken
    private String accessToken;
}
