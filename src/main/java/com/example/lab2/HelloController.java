package com.example.lab2;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class FileCheckThread implements Callable<ArrayList<String>> {
    ArrayList<File> filesMas = new ArrayList<File>();
    String searchString;
    ArrayList<String> filesNames = new ArrayList<String>();
    public boolean done = false; //Ну я мог бы сделать иначе. Да и вообще сделать не callable, а просто поток, ноооо...

    public void setSearchString(String searchString){
        this.searchString = searchString;
    }

    public void addFile(File file){
        this.filesMas.add(file);
    }

    @Override
    public ArrayList<String> call()
    {
        filesNames = new ArrayList<String>();
        Scanner scanner = new Scanner("");
        long startTime = System.nanoTime();//Начало засечки времени
        for(int i = 0; i < filesMas.size(); i++){
            try{
                scanner = new Scanner(filesMas.get(i), StandardCharsets.UTF_8); //Ну тут просто файл считываем
            }
            catch (Exception e){

            }
            while(scanner.hasNextLine()){
                if(scanner.nextLine().contains(searchString)){
                    filesNames.add(filesMas.get(i).getName());
                    break;
                }
            }
            scanner.close();
        }
        long endTime = System.nanoTime();
        System.out.println("starttime: " + String.valueOf(startTime) + ", endtime: " + String.valueOf(endTime) + ", difference: " + String.valueOf(endTime - startTime));
        return filesNames;
    }
}

public class HelloController {

    @FXML
    private TextField searchStringField; //Поле ввода искомой строки
    @FXML
    private Label outputLabel; //Поле вывода наименований файлов

    private int numOfThreads = 4;

    @FXML
    protected void startProcessButtonClick() {
        chooseTheFiles();
    }

    protected void chooseTheFiles() {
        Frame chooseFrame = new Frame();
        chooseFrame.setSize(400, 400);
        FileDialog fd = new FileDialog(chooseFrame, "Выберите файлы", FileDialog.LOAD);
        fd.setMultipleMode(true);
        fd.setVisible(true);

        startTheSearch(fd.getFiles(), searchStringField.getText());
    }

    protected void startTheSearch(File[] filesMas, String searchString) {
        ExecutorService pool = Executors.newFixedThreadPool(numOfThreads);

        FileCheckThread[] tasks = new FileCheckThread[numOfThreads];
        for (int i = 0; i < numOfThreads; i++){ //Здесь создаём потоки и ставим искомую строку
            FileCheckThread temp = new FileCheckThread();
            temp.setSearchString(searchString); //Вообще, вполне вероятно .что можно было сделать проще. Но я не понимаю как
            tasks[i] = temp;
        }
        for (int i = 0; i < filesMas.length; i++){ //Добавляем в каждый поток равномерно файлов.
            tasks[i%numOfThreads].addFile(filesMas[i]);
        }

        Future<ArrayList<String>>[] futures = new Future[numOfThreads];
        for (int i = 0; i < numOfThreads; i++){
            futures[i] = pool.submit(tasks[i]);
        }

        ArrayList<String> filesNames = new ArrayList<String>(); //Выводимые в конце имена
        int endNum = 0; //Для учёта закончивших потоков
        while(endNum < numOfThreads){
            for (int i = 0; i < numOfThreads; i++){  //Вообще, чтобы не проходить закончившие потоки раз за разом, можно изменить коллекцию с ними. Но там свои нюансы
                if (futures[i].isDone() && !tasks[i].done){ //Если он закончил один раз.
                    try{
                        ArrayList<String> returnValue = futures[i].get();
                        for (int j = 0; j < returnValue.size(); j++){
                            filesNames.add(returnValue.get(j));
                        }
                    }
                    catch(Exception e){
                        e.printStackTrace(); //break или return не ставил, чтобы остальные потоки попытались закончить.
                    }
                    tasks[i].done = true; //Ставим true - один раз точно прошёл по этому потоку
                    endNum++;
                }
            }
        }

        outputLabel.setText("");
        for (int i = 0; i < filesNames.size(); i++){
            outputLabel.setText(outputLabel.getText() + filesNames.get(i) + ",");
            //System.out.println(filesNames.get(i));
        }

        pool.shutdown();

        Scanner scanner = new Scanner("");
        long startTime = System.nanoTime();
        for(int i = 0; i < filesMas.length; i++){
            try{
                scanner = new Scanner(filesMas[i], StandardCharsets.UTF_8); //Ну тут просто файл считываем
            }
            catch (Exception e){

            }
            while(scanner.hasNextLine()){
                if(scanner.nextLine().contains(searchString)){
                    filesNames.add(filesMas[i].getName());
                    break;
                }
            }
            scanner.close();
        }
        long endTime = System.nanoTime();
        System.out.println("starttime: " + String.valueOf(startTime) + ", endtime: " + String.valueOf(endTime) + ", difference: " + String.valueOf(endTime - startTime));
    }
}