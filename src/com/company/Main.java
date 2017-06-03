package com.company;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class Main {

    private static LinkedList<HashMap<String, byte[]>> files = new LinkedList<>();
    private static String SOURCE;
    private static String DEST;
    private static int PERIOD_OF_TIME;
    private static boolean isEnded = false;
    private static int MAX_SIZE;

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
                    HashMap<String, byte[]> map = loadFromFile(SOURCE);
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
        while (num > 0) {
            HashMap<String, byte[]> map = files.pollFirst();
            saveToFile(map, DEST + num);
            num--;
        }

        sc.close();
    }


    // save File in memory
    public static HashMap<String, byte[]> loadFromFile(String source) {
        HashMap<String, byte[]> map = null;
        HashMap<String, byte[]> previousMap = null;
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
                FileInputStream in = new FileInputStream(str);
                byte[] bytes = new byte[in.available()];
                if (previousMap != null)
                    prevBytes = previousMap.get(str);
                in.read(bytes);
                in.close();
                if (prevBytes != null && Arrays.equals(bytes, prevBytes)) {
                    map.put(str, prevBytes);
                } else {
                    map.put(str, bytes);
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
    public static void saveToFile(HashMap<String, byte[]> map, String dest) throws Exception {
        for (Map.Entry entry : map.entrySet()) {
            String path = (String) entry.getKey();
            String[] strings = path.split(SOURCE);
            String name = strings[1];
            Path absolutePath = Paths.get(dest + File.separator + name);
            if (Files.notExists(absolutePath.getParent()))
                Files.createDirectories(absolutePath.getParent());
            FileOutputStream out = new FileOutputStream(absolutePath.toString());
            out.write((byte[]) entry.getValue());
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
