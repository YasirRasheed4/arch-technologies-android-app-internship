# College Alert

An Android app (Kotlin + Jetpack Compose) that keeps students informed about campus events.
Built for Arch Technologies - Android App Development internship, Month 1, Task 1.

## Features
- Add, edit and delete campus events (title, description, location, category, date, time)
- Upcoming / Past tabs, category filter chips and live search
- Reminder notifications (none, at start, 15 min, 30 min, 1 hour, 1 day before)
- Reminders are re-scheduled automatically after the phone restarts
- Star important events; countdown text such as "in 2 d"
- Local storage with Room (works fully offline); sample events are added on first launch

## Run it
1. Install Android Studio (Koala / 2024.1 or newer) with JDK 17.
2. File > Open > select the `CollegeAlert` folder and wait for Gradle sync.
3. Create an emulator (Pixel, API 34) or plug in a phone with USB debugging.
4. Press Run. Allow notifications when asked (Android 13+).
5. To test a reminder quickly: add an event 2 minutes ahead with the "At start" reminder.

## Structure
```
app/src/main/java/com/collegealert/
  MainActivity.kt          navigation + permission request
  EventViewModel.kt        state, save/delete, sample data
  data/                    CampusEvent (entity), EventDao, AppDatabase
  notify/                  ReminderScheduler, ReminderReceiver, BootReceiver, NotificationHelper
  ui/                      Theme, Formatters, EventListScreen, EventDetailScreen, EventFormScreen
```
