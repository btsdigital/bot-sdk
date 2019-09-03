module kz.btsd.bot.botsdk {

    exports kz.btsd.bot.botsdk;
    uses kz.btsd.bot.botsdk.LongPollingBot;

    requires kotlin.stdlib;
    requires com.fasterxml.jackson.databind;
    requires bot.api.contract;
    requires kotlin.logging;
    requires okhttp3;
    requires okhttp3.logging;
    requires retrofit2;
    requires retrofit2.converter.jackson;
    requires com.fasterxml.jackson.core;
    opens kz.btsd.bot.botsdk;
}