package tutorial;

import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Bot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "Djalmabot";
    }

    @Override
    public String getBotToken() {
        Dotenv dotenv = Dotenv.configure().directory("./").load();
        return dotenv.get("APIKEY");
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message msg = update.getMessage();
        User user = msg.getFrom();
        Long id = user.getId();
        System.out.println("ID de usuario: " + id);
        copyMessage(id, msg.getMessageId());
    }

    public void sendText(Long who, String what) {
        SendMessage sm = SendMessage
            .builder()
            .chatId(who.toString()) // Who are we sending a message to
            .text(what)
            .parseMode("HTML")
            .build(); // Message content
        try {
            execute(sm); // Actually sending the message
        } catch (TelegramApiException e) {
            System.out.println(e);
            throw new RuntimeException(e); // Any error will be printed here
        }
    }

    public void copyMessage(Long who, Integer msgId) {
        CopyMessage cm = CopyMessage
            .builder()
            .fromChatId(who.toString()) // We copy from the user
            .chatId(who.toString()) // And send it back to him
            .messageId(msgId) // Specifying what message
            .build();
        try {
            execute(cm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
