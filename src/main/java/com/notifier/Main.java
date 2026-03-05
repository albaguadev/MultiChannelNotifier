package main.java.com.notifier;

import main.java.com.notifier.context.NotificationContext;
import main.java.com.notifier.strategy.EmailStrategy;

public class Main {
    public static void main(String[] args) {

        NotificationContext context = new NotificationContext();

        context.setStrategy(new EmailStrategy());
        context.executeStrategy("aguadoalbert@gmail.com ha escrito esto");


    }
}