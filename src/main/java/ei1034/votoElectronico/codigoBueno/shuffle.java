package ei1034.votoElectronico.codigoBueno;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class shuffle {

    private static SecureRandom sr = new SecureRandom();

    public static void main(String[] args) {
        List<String> students = Arrays.asList("Foo", "Bar", "Baz", "Qux");

        int primero = sr.nextInt(4) + 1;

        int segundo = sr.nextInt(4) + 1;
        while (segundo == primero) {
            segundo = sr.nextInt(4) + 1;
        }

        int tercero = sr.nextInt(4) + 1;
        while (tercero == primero || tercero == segundo) {
            tercero = sr.nextInt(4) + 1;
        }

        int cuarto = 10 - primero - segundo - tercero;

        List<String> newStudents = new ArrayList<String>();

        newStudents.add(students.get(primero - 1));
        newStudents.add(students.get(segundo - 1));
        newStudents.add(students.get(tercero - 1));
        newStudents.add(students.get(cuarto - 1));

        System.out.println(students);
        System.out.println(newStudents);
    }
}
