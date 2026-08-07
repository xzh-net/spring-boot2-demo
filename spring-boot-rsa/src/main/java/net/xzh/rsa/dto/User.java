package net.xzh.rsa.dto;

/**
 * 用户实体类，包含用户基本信息。
 * <p>
 * 作为业务数据在加密请求/响应中传输的示例实体。
 * </p>
 */
public class User {
    /** 用户唯一标识 */
    private Long id;
    /** 用户姓名 */
    private String name;
    /** 用户邮箱 */
    private String email;
    /** 用户年龄 */
    private int age;

    public User() {
    }

    public User(Long id, String name, String email, int age) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
