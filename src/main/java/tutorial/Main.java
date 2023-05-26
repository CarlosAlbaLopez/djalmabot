package tutorial;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public final class Main {

    private Main() {}

    public static void main(String[] args) throws TelegramApiException, SchedulerException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        Bot bot = new Bot();
        botsApi.registerBot(bot);
        Cron.registerBot(bot);

        try {
            JobDetail j = JobBuilder.newJob(Cron.class).build();

            Trigger t = TriggerBuilder
                .newTrigger()
                .withIdentity("CronTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0/5 * * * ?"))
                .build();

            Scheduler s = StdSchedulerFactory.getDefaultScheduler();

            s.start();
            s.scheduleJob(j, t);
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }
}
