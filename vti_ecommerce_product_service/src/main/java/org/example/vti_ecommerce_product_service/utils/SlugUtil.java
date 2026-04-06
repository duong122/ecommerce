package org.example.vti_ecommerce_product_service.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class SlugUtil {
    private SlugUtil() {}

    public static String toSlug(String input) {
        
        if (input == null) return "";

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        String withoutAccents = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
                                .matcher(normalized).replaceAll("");

        return withoutAccents.toLowerCase()
                        .replaceAll("[^a-z0-9\\s-]", "")       
                        .trim()
                        .replaceAll("\\s+", "-");                 
    }
}
