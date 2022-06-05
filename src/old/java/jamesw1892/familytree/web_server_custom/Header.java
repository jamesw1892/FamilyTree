package web_server_custom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * ______________________________________header__________________________________________
 * __________________________firstLine_________________________________
 *        ____________halfURL_________________________
 *        ___________path___________
 *                _____basename_____
 * method dirname/filename.extension?queryStr#fragment protocol/version[newline]fieldsStr
 * 
 * `query` is `HashMap` version of `queryStr` and `fields` is `HashMap` version of `fieldsStr`
 * where `queryStr` is `key1=val1&key2=val2` etc and `fieldsStr` is `key1: val1[newline]key2: val2` etc
 * 
 * data is the data appearing after the header, confusingly part of this class
 */
public class Header {
    private String header;
    private String firstLine;
    private String method;
    private String halfURL;
    private String path;
    private String dirname;
    private String basename;
    private String filename;
    private String extension;
    private String queryStr;
    private HashMap<String, String> query;
    private String fragment;
    private String protocol;
    private String version;
    private String fieldsStr;
    private HashMap<String, String> fields;
    private String data = null;
    private boolean makeWork;

    /**
     * 
     * @param socket
     * @param makeWork Whether to not throw errors and make it work if
     * possible even if that means making parts of the header empty
     * when they weren't but they were formatted incorrectly
     * @throws IOException
     */
    public Header(Socket socket, boolean makeWork) throws IOException {
        this.makeWork = makeWork;

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ArrayList<String> headerLines = new ArrayList<>();
        String line;

        // read all header lines into the array list and parse
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            headerLines.add(line);
        }
        this.parse(headerLines);

        line = "";

        // Now we need to read the post data if there is any
        // We can't read until EOF because EOF is only given if the socket is
        // closed and web browsers don't do this
        // So instead we use content-length to read that many characters
        if (this.fields.containsKey("Content-Length")) {
            int contentLength = Integer.valueOf(this.fields.get("Content-Length"));
            for (int dummy = 0; dummy < contentLength; dummy++) {
                char chr = (char) in.read();
                line += chr;
            }

            // decode if encoded as expected
            if (this.fields.get("Content-Type").equals("application/x-www-form-urlencoded")) {
                this.data = URLDecoder.decode(line, StandardCharsets.UTF_8.toString());
            }
            // if not encoded like this then not implemented so ignore and leave this.data as null
        }
        // don't close buffered reader as will close the socket
    }

    private void parse(ArrayList<String> lines) {

        if (lines.isEmpty()) {
            if (this.makeWork) {
                lines.add("");
            } else {
                throw new IllegalArgumentException("Invalid header, cannot be empty");
            }
        }

        this.header = String.join(System.lineSeparator(), lines);

        // first line
        this.firstLine = lines.get(0);
        String[] firstLineSplit = this.firstLine.split(" ");
        if (firstLineSplit.length != 3) {
            if (this.makeWork) {
                firstLineSplit = new String[] {"GET", "/", "HTTP/1.1"};
            } else {
                throw new IllegalArgumentException("Invalid header, the first line must have the method, a space, the halfURL, another space and the protocol and version");
            }
        }
        this.method = firstLineSplit[0].toUpperCase();
        this.parseHalfURL(firstLineSplit[1]);
        String[] protocolAndVersion = firstLineSplit[2].split("/", -1);
        if (protocolAndVersion.length != 2) {
            if (this.makeWork) {
                protocolAndVersion = new String[] {"HTTP", "1.1"};
            } else {
                throw new IllegalArgumentException("Invalid header, the protocol and version must be separated by a forward slash");
            }
        }
        this.protocol = protocolAndVersion[0];
        this.version = protocolAndVersion[1];

        // subsequent lines
        this.fieldsStr = "";
        this.fields = new HashMap<>();
        for (int i = 1; i < lines.size(); i++) {
            this.fieldsStr += System.lineSeparator() + lines.get(i);
            String[] pair = lines.get(i).split(": ", 2);
            if (pair.length != 2) {
                if (!this.makeWork) {
                    throw new IllegalArgumentException("Invalid header, each line apart the first must contain a header key, ': ' and the corresponding value");
                }
            } else {
                this.fields.put(pair[0], pair[1]);
            }
        }

        if (lines.size() > 1) {
            this.fieldsStr = this.fieldsStr.substring(System.lineSeparator().length());
        }
    }

    private void parseHalfURL(String halfURL) {

        // precondition: not empty
        this.halfURL = halfURL;

        String[] pathAndRest = this.halfURL.split("\\?");

        switch (pathAndRest.length) {
            case 2:
                String[] queryAndFragment = pathAndRest[1].split("#");
                this.parsePath(pathAndRest[0]);
                if (queryAndFragment.length == 0) {
                    this.parseQuery("");
                    this.parseFragment(new String[1]);
                } else {
                    this.parseQuery(queryAndFragment[0]);
                    this.parseFragment(queryAndFragment);
                }
                break;

            case 1:
                String[] pathAndFragment = pathAndRest[0].split("#");
                if (pathAndFragment.length == 0) {
                    if (this.makeWork) {
                        this.parsePath("/");
                    } else {
                        throw new IllegalArgumentException("Invalid header, must provide a path");
                    }
                } else {
                    this.parsePath(pathAndFragment[0]);
                }
                this.parseQuery("");
                this.parseFragment(pathAndFragment);
                break;

            case 0:
                // only happens when the only character was '?'
                if (!this.makeWork) {
                    throw new IllegalArgumentException("Invalid header, halfURL must contain a path");
                }
                // else carry to default case below

            default:
                if (this.makeWork) {
                    this.parsePath("/");
                    this.parseQuery("");
                    this.parseFragment(new String[1]);
                } else {
                    throw new IllegalArgumentException("Invalid header, halfURL must not contain more than one question mark");
                }
        }
    }

    private void parseFragment(String[] somethingAndFragment) {
        switch (somethingAndFragment.length) {
            case 2:
                this.fragment = somethingAndFragment[1];
                break;
            case 1:
                this.fragment = "";
                break;
            default:
                if (this.makeWork) {
                    this.fragment = "";
                } else {
                    throw new IllegalArgumentException("Invalid header, must not contain more than one '#'");
                }
        }
    }

    private void parseQuery(String q) {
        this.queryStr = q;
        this.query = this.parseUrlList(q);
    }

    /**
     * Return a dictionary of fields and values in the url list of the form
     * field1=value1&field2=value2
     */
    private HashMap<String, String> parseUrlList(String listStr) {
        HashMap<String, String> listMap = new HashMap<>();

        if (listStr != null && !listStr.isEmpty()) {
            for (String pair: listStr.split("&")) {
                String[] pairSplit = pair.split("=");
                switch (pairSplit.length) {
                    case 2:
                        listMap.put(pairSplit[0], pairSplit[1]);
                        break;
                    case 1:
                        listMap.put(pairSplit[0], "");
                        break;
                    case 0:
                        // only happens when 'listStr' only contain '&'s
                        if (!this.makeWork) {
                            throw new IllegalArgumentException("Invalid header, query pair must contain a non-empty key");
                        }
                    default:
                        if (!this.makeWork) {
                            throw new IllegalArgumentException("Invalid header, query pair must not contain more than one '='");
                        }
                }
            }
        }

        return listMap;
    }

    /**
     * Parse a path for its directory name, base filename with extension,
     * extension only and filename without extension.
     * Like PHP function `pathinfo`
     * @param path
     */
    private void parsePath(String path) {

        this.path = path.replace("\\", "/");

        if (this.path.isEmpty() || this.path.charAt(0) != '/') {
            this.path = "/" + this.path;
        }

        String[] dirnameAndBasename = splitOnLast("/", this.path);
        this.dirname = dirnameAndBasename[0];
        if (this.dirname.isEmpty()) {
            this.dirname = "/";
        }
        this.basename = dirnameAndBasename[1];

        String[] filenameAndExtension = splitOnLast(".", this.basename);
        this.filename = filenameAndExtension[0];
        this.extension = filenameAndExtension[1];
    }

    /**
     * Will always return a string of length 2 but each may be empty
     */
    private static String[] splitOnLast(String delimiter, String s) {

        int index = s.lastIndexOf(delimiter);
        if (index == -1) {
            return new String[] {s, ""};
        }

        return new String[] {s.substring(0, index), s.substring(index + 1)};
    }

    // -------------------- GETTERS ------------------------------

	public String getHeader() {
		return this.header;
    }

	public String getFirstLine() {
		return this.firstLine;
	}

	public String getMethod() {
		return this.method;
	}

	public String getHalfURL() {
		return this.halfURL;
	}

	public String getPath() {
		return this.path;
	}

	public String getDirname() {
		return this.dirname;
	}

	public String getBasename() {
		return this.basename;
	}

	public String getFilename() {
		return this.filename;
	}

	public String getExtension() {
		return this.extension;
    }

    public String getQueryStr() {
        return this.queryStr;
    }

	public HashMap<String, String> getQuery() {
		return this.query;
	}

	public String getFragment() {
		return this.fragment;
	}

	public String getProtocol() {
		return this.protocol;
	}

	public String getVersion() {
		return this.version;
    }
    
    public String getFieldsStr() {
        return this.fieldsStr;
    }

	public HashMap<String, String> getFields() {
		return this.fields;
    }

    public String getData() {
        return this.data;
    }

	public String toString() {
		return this.header;
    }

    // ------------------ TESTING --------------------------

    private Header(ArrayList<String> header, boolean makeWork) {
        this.parse(header);
        this.makeWork = makeWork;
    }

    private static String hashMap2Str(HashMap<String, String> hashMap, String withinPairDelim, String betweenPairsDelim) {
        String out = "";
        for (String key: hashMap.keySet()) {
            out += betweenPairsDelim + key + withinPairDelim + hashMap.get(key);
        }

        return out.substring(betweenPairsDelim.length());
    }

    private static ArrayList<String> lines(String s) {
        ArrayList<String> out = new ArrayList<>();
        for (String l: s.split("\n")) {
            out.add(l);
        }
        return out;
    }

    /**
     * Takes them apart and puts them back together again
     * @param args
     */
    public static void main(String[] args) {

        // TODO:
        // - move to tests
        // - test edge cases

        String[] headers = {
            "GET /www/htdocs/inc/lib.inc.php?arg=value#anchor HTTP/1.1\nUser-Agent: yolo",
            "GET /path/emptyextension.?arg=value#anchor HTTP/1.1\nUser-Agent: yolo",
            "GET /path/noextension?arg=value#anchor HTTP/1.1\nUser-Agent: yolo",
            "GET /some/path/.test?arg=value#anchor HTTP/1.1"
        };

        for (String header: headers) {
            Header o = new Header(lines(header), false);

            System.out.println();
            System.out.println();
            System.out.println("Testing:" + System.lineSeparator() + header.replace("\n", System.lineSeparator()));
            System.out.println();
            System.out.println(o.method + " " + o.dirname + "/" + o.filename + "." + o.extension + "?" + hashMap2Str(o.query, "=", "&") + "#" + o.fragment + " " + o.protocol + "/" + o.version + System.lineSeparator() + hashMap2Str(o.fields, ": ", System.lineSeparator()));
        }

        System.out.println("'" + "###".split("#").length + "'");
        System.out.println(splitOnLast("/", "/a").length);
        String[] tests = {
            "/path",
            "",
            "/www/htdocs/inc/lib.inc.php",
            "/path/emptyextension.",
            "/path/noextension",
            "/some/path/.test",
            "/"
        };

        for (String path: tests) {

            System.out.println("\n\n'" + path + "':");
            path = path.replace("\\", "/");
            if (path.isEmpty() || path.charAt(0) != '/') {
                path = "/" + path;
            }
            String[] dirnameAndBasename = splitOnLast("/", path);
            if (dirnameAndBasename[0].isEmpty()) {
                dirnameAndBasename[0] = "/";
            }
            String[] filenameAndExtension = splitOnLast(".", dirnameAndBasename[1]);
            System.out.println("dirname: '" + dirnameAndBasename[0] + "'");
            System.out.println("basename: '" + dirnameAndBasename[1] + "'");
            System.out.println("filename: '" + filenameAndExtension[0] + "'");
            System.out.println("extension: '" + filenameAndExtension[1] + "'");
        }
    }
}