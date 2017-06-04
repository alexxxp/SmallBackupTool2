package com.company;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;


public class Main {

    private static LinkedList<HashMap<String, SimpleFile>> files = new LinkedList<>();
    private static LinkedList<Date> dates = new LinkedList<>();
    private static String SOURCE;
    private static String DEST;
    private static int PERIOD_OF_TIME;
    private static boolean isEnded = false;
    private static int MAX_SIZE;
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss - dd.MM.yy", Locale.GERMAN);

    public static void main(String[] args) throws Exception {
        SOURCE = args[0];
        DEST = args[1];
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter how often program must save File in seconds:");
        PERIOD_OF_TIME = sc.nextInt();
        System.out.println("Enter number of copies to store:");
        MAX_SIZE = sc.nextInt();

        Thread secondThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isEnded) {
                    HashMap<String, SimpleFile> map = loadFromFile(SOURCE);
                    if (files.size() >= MAX_SIZE)
                        files.removeLast();
                    if (map.size() != 0)
                        files.addFirst(map);
                    try {
                        Thread.sleep(PERIOD_OF_TIME * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        secondThread.start();

        String s = "";
        System.out.println("Program will save File in memory every " + PERIOD_OF_TIME + " seconds");
        System.out.println();
        System.out.println("Enter q to stop saving:");
        while (!s.equals("q")) {
            s = sc.nextLine();
        }
        isEnded = true;

        System.out.println("Enter how many backups to save on hard drive, available " + files.size() + ":");
        int num = sc.nextInt();
        String sourceName = SOURCE.substring(SOURCE.lastIndexOf(File.separator));
        while (num > 0) {
            HashMap<String, SimpleFile> map = files.pollFirst();
            Date date = dates.pollFirst(); //get dates from end of the list
            saveToFile(map, DEST + sourceName + " - " + simpleDateFormat.format(date));
            num--;
        }

        sc.close();
    }


    // save File in memory
    public static HashMap<String, SimpleFile> loadFromFile(String source) {
        if (dates.size() >= MAX_SIZE)
            dates.removeLast();
        dates.add(new Date());

        HashMap<String, SimpleFile> map = null;
        HashMap<String, SimpleFile> previousMap = null;
        List<String> list = null;

        try {
            list = getFileTree(source);
            map = new HashMap<>(list.size() + 1);
            byte[] prevBytes = null;
            if (!files.isEmpty()) {
                previousMap = files.peekFirst();
            }

            setWritable(list, false);

            for (String str : list) {
                File file = new File(str);
                long lastModified = file.lastModified();

                if (previousMap != null) {
                    long previousLastModified = previousMap.get(str).getLastModified();
                    if (previousLastModified != 0 && previousLastModified == lastModified) {
                        map.put(str, previousMap.get(str));
                        continue;
                    }
                }

                FileInputStream in = new FileInputStream(str);
                byte[] bytes = new byte[in.available()];
                if (previousMap != null)
                    prevBytes = previousMap.get(str).getContent();
                in.read(bytes);
                in.close();
                if (prevBytes != null && Arrays.equals(bytes, prevBytes)) {
                    map.put(str, previousMap.get(str));
                } else {
                    map.put(str, new SimpleFile(str, bytes, lastModified));
                }
            }
            setWritable(list, true);
        } catch (IOException e) {
            setWritable(list, true);
            System.out.println(e.getMessage());
        }
        return map;
    }

    // write File to disk in DEST File
    public static void saveToFile(HashMap<String, SimpleFile> map, String dest) throws Exception {
        for (Map.Entry entry : map.entrySet()) {
            String path = (String) entry.getKey();
            String[] strings = path.split(SOURCE);
            String name = strings[1];
            SimpleFile simpleFile = (SimpleFile) entry.getValue();
            Path absolutePath = Paths.get(dest + File.separator + name);
            if (Files.notExists(absolutePath.getParent()))
                Files.createDirectories(absolutePath.getParent());
            FileOutputStream out = new FileOutputStream(absolutePath.toString());
            out.write(simpleFile.getContent());
            out.close();
        }
    }

    // get all files in SOURCE
    public static List<String> getFileTree(String root) throws IOException {
        ArrayList<String> list = new ArrayList<>();

        Queue<File> queue = new LinkedList<>();
        File rootFile = new File(root);
        queue.add(rootFile);

        while (!queue.isEmpty()) {
            File file = queue.poll();
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files == null) {
                    continue;
                } else {
                    for (int i = 0; i < files.length; i++) {
                        queue.add(files[i]);
                    }
                }
            } else {
                list.add(file.getAbsolutePath());
            }
        }
        return list;
    }

    //set folder or file writable
    public static void setWritable(List<String> list, boolean mode) {
        for (String str : list) {
            new File(str).setWritable(mode, false);
        }
    }
}
