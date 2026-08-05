package com.example.bot;

public enum UsersStatus {
    WAIT_NEW_COMMAND,
    WAIT_MESSAGE, // Для команды /help
    WAIT_BET, // Ввод ставки в начале игры
    GAME_WAIT_INSURANCE,
    GAME_CHOICE1, // Выбор игрока после сдачи двух карт
    GAME_REPEAT_CHOICE1, // Выбор игрока, если он решил добирать карты
    // Дальше если в GAME_CHOICE1 была выбрана цифра 3, т.е. разделение текущей руки на 2 игры
    GAME_CHOICE21, // Выбор, что делать с рукой в первой руке
    GAME_CHOICE22, // Выбор, что делать с рукой во второй руке
    GAME_REPEAT_CHOICE21, // Выбор игрока, если он решил добирать карты в первой колоде
    GAME_REPEAT_CHOICE22 // Выбор игрока, если он решил добирать карты во второй колоде
}
