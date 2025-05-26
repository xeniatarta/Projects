package datamodel;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;


public class Employee implements Serializable {
    private int idEmployee;
    private String name;

    public Employee(int idEmployee, String name) {
        this.idEmployee = idEmployee;
        this.name = name;
    }

    public int getIdEmployee() {

        return idEmployee;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Employee{" + "idEmployee=" + idEmployee + ", name='" + name + '\'' + '}';
    }
}