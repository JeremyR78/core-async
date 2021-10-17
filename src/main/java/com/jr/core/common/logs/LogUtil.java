package com.jr.core.common.logs;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LogUtil {


    // --------------------------------------
    // -    ATTRIBUTES                      -
    // --------------------------------------

    public static final Integer  SIZE_MAX_SUMMARY   = 80;
    public static final String   START_TAG_SUMMARY  = "##";
    public static final String   END_TAG_SUMMARY    = "##";
    public static final String   START_TAG_SERVICE  = "[";
    public static final String   END_TAG_SERVICE    = "]";

    public static final Pattern REGEX_TAG_FOUND = Pattern.compile("\\"+START_TAG_SERVICE+"(;*?)\\"+END_TAG_SERVICE);

    private LogUtil(){}

    // --------------------------------------
    // -            Methods                 -
    // --------------------------------------

    /**
     * Formatage des données à logger
     *
     * @param summary : Le résumé du log
     * @return
     */
    public static String format( String service, String subservice, String summary )
    {
        summary = validSizeSummary( summary );
        summary = replaceCharReturn( summary );
        return String.format("%s%s%s%s%s%s %s %s %s",
                START_TAG_SERVICE, service, END_TAG_SERVICE,
                START_TAG_SERVICE, subservice, END_TAG_SERVICE,
                START_TAG_SUMMARY, summary, END_TAG_SUMMARY);
    }

    /**
     * Formatage des données à logger
     *
     * @param summary : Le résumé du log
     * @param content : La description
     * @return
     */
    public static String format( String service, String subservice, String summary, String content )
    {
        summary = validSizeSummary( summary );
        summary = replaceCharReturn( summary );
        return String.format("%s%s%s%s%s%s %s %s %s %s",
                START_TAG_SERVICE, service, END_TAG_SERVICE,
                START_TAG_SERVICE, subservice, END_TAG_SERVICE,
                START_TAG_SUMMARY, summary, END_TAG_SUMMARY,
                replaceCharReturn( content ));
    }

    /**
     * Formatage des données à logger
     *
     * @param summary   : Le résumé du log
     * @param exception : L'exception à logger
     * @return
     */
    public static String format( String service, String subservice, String summary, Throwable exception )
    {
        summary = validSizeSummary( summary );
        summary = replaceCharReturn( summary );
        if( exception == null ) {
            return format( service, subservice, summary );
        }
        return String.format("%s%s%s%s%s%s %s %s %s EXCEPTION : %s - CAUSE : %s - DETAILS : %s",
                START_TAG_SERVICE, service, END_TAG_SERVICE,
                START_TAG_SERVICE, subservice, END_TAG_SERVICE,
                START_TAG_SUMMARY, summary, END_TAG_SUMMARY,
                replaceCharReturn( exception.toString() ),
                replaceCharReturn( findFirstMessageCause( exception ) ),
                findFirstStackTraceCause( exception ));
    }

    /**
     * Formatage des données à logger
     *
     * @param summary : Le résumé du log
     * @param content : La description
     * @param exception  : L'exception à logger
     * @return
     */
    public static String format( String service, String subservice, String summary, String content, Throwable exception )
    {
        summary = validSizeSummary( summary );
        summary = replaceCharReturn( summary );
        if( exception == null ){
            return format( service, subservice, summary, content );
        }
        return String.format("%s%s%s%s%s%s %s %s %s %s -- EXCEPTION : %s - CAUSE : %s - DETAILS : %s",
                START_TAG_SERVICE, service, END_TAG_SERVICE,
                START_TAG_SERVICE, subservice, END_TAG_SERVICE,
                START_TAG_SUMMARY, summary, END_TAG_SUMMARY,
                replaceCharReturn( content ),
                replaceCharReturn( exception.toString() ),
                replaceCharReturn( findFirstMessageCause( exception ) ),
                findFirstStackTraceCause( exception ));
    }

    /**
     *
     * @param exception
     * @return
     */
    public static String formatException( Throwable exception )
    {
        if( exception == null ){
            return null;
        }
        return String.format("EXCEPTION : %s - CAUSE : %s - DETAILS : %s",
                replaceCharReturn( exception.toString() ),
                replaceCharReturn( findFirstMessageCause( exception ) ),
                findFirstStackTraceCause( exception ) );
    }

    /**
     * La taille de la chaine de caractère
     *
     * @param testSize : La chaine de caractère
     * @param sizeMax  : La taille à respecter
     * @return
     */
    public static boolean sizeString( String testSize, int sizeMax )
    {
        if( testSize == null )
        {
            return true;
        }
        int sizeString = testSize.length();
        return sizeString <= sizeMax;
    }

    /**
     * Vérification de la taille du résumé.
     * Coupe la chaine de caratère si celle-ci est trop longue
     *
     * @param summary
     */
    private static String validSizeSummary(String summary)
    {
        return validSizeString( summary, SIZE_MAX_SUMMARY );
    }

    private static String validSizeString(String text, Integer size)
    {
        if( ! sizeString( text, size ) )
        {
            return text.substring(0, Math.min(text.length(), size ));
        }
        return text;
    }

    /**
     * Retire les retours à la ligne d'un texte
     * @param text
     * @return
     */
    private static String replaceCharReturn(String text)
    {
        if( text != null && ! text.isEmpty() )
        {
            return text.replace("\n", "").replace("\r", "");
        }
        return text;
    }

    /**
     *
     * @param name
     * @return
     */
    public static String formatName( String name )
    {
        if( name == null ){
            return  null;
        }
        String regex = String.format( "%s\\%s\\%s%s", START_TAG_SERVICE, START_TAG_SERVICE, END_TAG_SERVICE, END_TAG_SERVICE);
        name = name.replaceAll( regex , "");
        return String.format("%s%s%s", START_TAG_SERVICE, name, END_TAG_SERVICE);
    }

    /**
     * Récupére la première StackTrace
     * @param throwable
     * @return
     */
    public static List<StackTraceElement> findFirstStackTraceCause(Throwable throwable )
    {
        if( throwable == null ){
            return null;
        }
        Throwable cause = throwable.getCause();
        if( cause != null ){
            return findFirstStackTraceCause( cause );
        }
        return Arrays.asList(throwable.getStackTrace());
    }

    /**
     * Récupére le premier message d'exception
     * @param throwable
     * @return
     */
    public static String findFirstMessageCause( Throwable throwable )
    {
        if( throwable == null ){
            return null;
        }
        Throwable cause = throwable.getCause();
        if( cause != null ){
            return findFirstMessageCause( cause );
        }
        return throwable.toString();
    }


    public static boolean hasEverTag( String name ){
        if( name == null ) return false;
        Matcher m = REGEX_TAG_FOUND.matcher( name );
        return m.find();
    }

    public static String fLog( String name ){
        if( hasEverTag( name) ) return name;
        return String.format("%s%s%s",
                START_TAG_SERVICE, name, END_TAG_SERVICE);
    }

}
