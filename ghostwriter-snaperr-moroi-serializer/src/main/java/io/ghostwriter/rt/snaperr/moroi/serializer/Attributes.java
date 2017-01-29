package io.ghostwriter.rt.snaperr.moroi.serializer;

import com.google.gson.annotations.SerializedName;

/*

{
	"data" : {
		"type" : "exception",
		"attributes" : {
			"application_uuid" : "abcd1234",
			"file" : "ErrenousSource.class",
			"original_timestamp" : "1474577179",
			"exception" : "NullPointerException",
			"stacktrace" : "javax.servlet.ServletException: Something bad happened\n at com.example.myproject.OpenSessionInViewFilter.doFilter(OpenSessionInViewFilter.java:60)\n at org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1157)\n ... 27 more\n Caused by: org.hibernate.exception.ConstraintViolationException: could not insert: [com.example.myproject.MyEntity]\n at org.hibernate.exception.SQLStateConverter.convert(SQLStateConverter.java:96)\n at org.hibernate.exception.JDBCExceptionHelper.convert(JDBCExceptionHelper.java:66)\n ... 32 more\n Caused by: java.sql.SQLException: Violation of unique constraint MY_ENTITY_UK_1: duplicate value(s) for column(s) MY_COLUMN in statement [...]\n at org.hsqldb.jdbc.Util.throwError(Unknown Source)\n at org.hsqldb.jdbc.jdbcPreparedStatement.executeUpdate(Unknown Source)\n ... 54 more",
			"context" : {
				"data" : [{
						"class" : "OpenSessionInViewFilter",
						"method" : "doFilter",
						"variables" : [{
								"value" : "Easy as 123?",
								"type" : "Question",
								"name" : "question"
							}, {
								"value" : 1,
								"type" : "Integer",
								"name" : "iterator"
							}
						]
					}
				]
			},
			"instance" : "instance@1234",
			"line" : 1234
		}
	}
}

 */


public class Attributes {

    @SerializedName("application_uuid")
    private String applicationUUID;
    private String file;

    @SerializedName("original_timestamp")
    private long timestamp;

    private String exception;

    @SerializedName("stacktrace")
    private String stackTrace;

    private Context context = new Context();

    // TODO(snorbi07): rename to source - static classes do not have an
    // sourceOfError... needs to be discussed
    @SerializedName("instance")
    private Object sourceOfError;

    private int line;

    public String getApplicationUUID() {
        return applicationUUID;
    }

    public void setApplicationUUID(String applicationUUID) {
        this.applicationUUID = applicationUUID;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Object getSourceOfError() {
        return sourceOfError;
    }

    public void setSourceOfError(Object sourceOfError) {
        this.sourceOfError = sourceOfError;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

}