# Java-Based Online Quiz Platform

A desktop online quiz platform for Java programming with **timed quizzes**, **detailed
performance reports**, and full administration - built with **Java Swing**, stored as
**JSON files**, and compiled with plain **`javac`** (no external libraries, no build tool).

## Features

### Admin
- **User Management** - create / update / delete accounts and roles (Admin, Quiz Creator, Participant)
- **Quiz Content Management** - approve or reject quiz content submitted by creators (with reason)
- **System Settings** - platform name, default duration, pass %, max attempts, retakes, leaderboard toggle
- **Performance Reports** - bar / pie / line charts of averages, pass rate and attempt trends
- **System Alerts** - automatic notifications for users, submissions, approvals and attempts

### Quiz Creator
- **Quiz Creation** - wizard to build quizzes (title, topic, duration, questions, options, correct answers, points)
- **Quiz Results** - review each submission, adjust scoring and write feedback
- **Participant Interactions** - message participants
- **Quiz History** - full log of drafts, submissions, approvals and rejections
- **Performance Overview** - charts for averages, attempts received and pass/fail

### Participant
- **Take Quizzes** - timed quizzes with countdown, navigation and **auto-submit on expiry**
- **Participation History** - table of all attempts with per-question breakdown
- **Performance Report** - overview cards, score-trend and per-quiz charts, feedback
- **Interactions** - message quiz creators / admins
- **Quiz Reminders** - create and manage reminders for upcoming quizzes
- **Leaderboard** - overall rankings with personal standing highlighted

## Run

Windows:

```
run.bat
```

Linux / macOS:

```
bash run.sh
```

Manual:

```
javac -encoding UTF-8 -d out (all files under src)
java -cp out quizplatform.Main
```

Requires JDK 17+ (developed and tested on JDK 25).

## Demo accounts

| Role       | Email               | Password   |
|------------|---------------------|------------|
| Admin      | admin@quiz.local    | admin123   |
| Quiz Creator | creator@quiz.local | creator123 |
| Participant  | alice@quiz.local   | pass123    |
| Participant  | bob@quiz.local     | pass123    |

Accounts are created by the administrator; data is seeded on first run.

## Project structure

```
src/quizplatform/
├── Main.java                  entry point
├── model/                     User, Quiz, Question, Attempt, Message, Reminder, Alert, SystemSettings
├── util/                      Json (dependency-free parser/writer), Hash (SHA-256)
├── storage/Repository.java    load/save everything under ./data
├── service/                   auth, users, quizzes, attempts, messages, reminders,
│                              alerts, reports, leaderboard  (AppContext wires them)
└── ui/                        LoginFrame, MainFrame, Ui theme helpers
    ├── components/ChartPanel  bar / pie / line charts drawn with Graphics2D
    ├── admin/                 user mgmt, approvals, settings, reports, alerts
    ├── creator/               quiz wizard, results/grading, history, overview
    ├── participant/           available quizzes, timed quiz frame, history,
    │                          performance, reminders, leaderboard
    └── common/MessagesPanel   shared interaction panel
data/                          created automatically (JSON files, one per quiz/attempt)
```

## Notes

- Passwords are stored as salted SHA-256 hashes (never in plain text).
- Quiz editing is only allowed while a quiz is Draft or Rejected.
- Scoring is automatic; creators can adjust the final score and add feedback per attempt.
- All writes are atomic (temp file + move), so data files are never left half-written.
