package au.org.ala.util;

import java.io.InputStreamReader;

public class TestUtils {
    protected String getResource(String name) throws Exception {
        InputStreamReader reader = null;

        try {
            reader = new InputStreamReader(this.getClass().getResourceAsStream(name));
            StringBuilder builder = new StringBuilder(1024);
            char[] buffer = new char[1024];
            int n;

            while ((n = reader.read(buffer)) >= 0) {
                if (n == 0)
                    Thread.sleep(100);
                else
                    builder.append(buffer, 0, n);
            }
            return builder.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }
}
