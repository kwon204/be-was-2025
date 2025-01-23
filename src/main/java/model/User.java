package model;

public class User {
    private static final String DEFAULT_IMAGE = "default_profile.jpeg";

    private String userId;
    private String password;
    private String name;
    private String email;
    private String imagePath;

    public User(String userId, String password, String name, String email, String imagePath) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.imagePath = imagePath;
        if (imagePath == null) {
            this.imagePath = DEFAULT_IMAGE;
        }
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getImagePath() {
        return imagePath;
    }

    @Override
    public String toString() {
        return "User [userId=" + userId + ", password=" + password + ", name=" + name + ", email=" + email + "]";
    }
}
