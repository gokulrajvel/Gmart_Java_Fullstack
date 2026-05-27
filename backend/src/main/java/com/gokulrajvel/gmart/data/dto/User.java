package com.gokulrajvel.gmart.data.dto;

import com.gokulrajvel.gmart.data.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = true)
    private String address;

    @Column(nullable = true)
    private String phone;

    @Column(name = "aadhar_no", nullable = true)
    private String aadharNo;

    public User() {}

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = null;
        this.phone = null;
        this.aadharNo = null;
    }

    public User(int id, String username, String password, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = null;
        this.phone = null;
        this.aadharNo = null;
    }

    public User(int id, String username, String password, Role role, String address, String phone, String aadharNo) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = address;
        this.phone = phone;
        this.aadharNo = aadharNo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAadharNo() { return aadharNo; }
    public void setAadharNo(String aadharNo) { this.aadharNo = aadharNo; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role=" + role + ", address='" + address + "', phone='" + phone + "', aadharNo='" + aadharNo + "'}";
    }
}
