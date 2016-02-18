package unidue.rc.system;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Universitaet Duisburg Essen
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by nils on 17.09.15.
 */
public class BookUtils {

    /**
     * Example signature:
     * <code>D00 HQLW23333(10)-C,9,2+100</code>
     */
    private static final Pattern MONOGRAPHY_SIGNATURE_PATTERN = Pattern.compile("(?<location>[DE][0-9]{2})?" +
            "(?:\\s*)" +
            "(?<notation>[a-zA-Z]{3,4}[0-9]{3,6})" +
            "(?:\\s*)" +
            "(?<edition>\\([0-9]{1,2}\\))?" +
            "(?:\\s*)" +
            "(?<volume>-([a-zA-Z]{1,2}|[0-9]{1,4})(,([a-zA-Z]{1,2}|[0-9]{1,4}))*)?" +
            "(?:\\s*)" +
            "(?<itemnumber>\\+[0-9]{1,3})?" +
            "(?:\\s*)" +
            "(_d)?");

    public static String getEdition(String signature) {
        String edition = getGroupNameValue(signature, "edition");
        return !StringUtils.isEmpty(edition)
                ? edition.substring(1, edition.length() - 1)
                : null;
    }

    public static String getVolume(String signature) {
        String volume = getGroupNameValue(signature, "volume");
        return !StringUtils.isEmpty(volume)
                ? volume.substring(1)
                : null;
    }

    /**
     * Returns the normalized form of a signature that contains notation, edition and volume
     * according to {@link #MONOGRAPHY_SIGNATURE_PATTERN}.
     * Ex. <code>D00 HQLW23333(10)-C,9,2+100 -&gt; HQLW23333(10)-C,9,2</code>
     *
     * @param signature signature that should be normalized
     * @return the normalized signature
     */
    public static String getNormalized(String signature) {
        String[] normalizedValues = {"notation", "edition", "volume"};
        List<String> signatureValues = Arrays.stream(normalizedValues)
                .map(groupName -> getGroupNameValue(signature, groupName))
                .filter(value -> value != null)
                .collect(Collectors.toList());
        return StringUtils.join(signatureValues.toArray());
    }

    public static boolean isMonographySignatureValid(String signature) {

        Matcher matcher = createMatcher(signature, MONOGRAPHY_SIGNATURE_PATTERN);
        return matcher.matches();
    }

    private static String getGroupNameValue(String signature, String groupName) {
        Matcher matcher = createMatcher(signature, MONOGRAPHY_SIGNATURE_PATTERN);
        return matcher.matches()
                ? matcher.group(groupName)
                : null;
    }

    private static Matcher createMatcher(String signature, Pattern pattern) {
        return pattern.matcher(signature);
    }
}
