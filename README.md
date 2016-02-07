HaxSync by Codarama
=======

This project is forked from the original [HaxSync](https://github.com/mots/haxsync) project by Mathias Roth.

HaxSync was initially developed with the puspose of better integration between Android devices and Facebook. Since then a lot has changed and there are multiple solutions out there that allow contacts syncing and calendar integrations.

However with the recent [changes](https://developers.facebook.com/docs/apps/upgrading#upgrading_v2_0_user_ids) to the Facebook API it [has become apparent](http://stackoverflow.com/questions/23417356/facebook-graph-api-v2-0-me-friends-returns-empty-or-only-friends-who-also-u) that joining the information from Facebook and Android / Google accounts has become much harder, the major limitation being that every friend you want to sync needs to have given permissions to the syncing application, otherwise his information would not be provided by the Facebook API.

This, however, is practically impossible for anybody with more than 5 friends.

Project Roadmap
---
1. Provide easy ways of importing Facebook contact lists
2. Provide easy to use interface to configure contacts sync
3. Provide advanced options for syncing intervals, etc.

Current state
---

[![Build Status](https://travis-ci.org/Codarama/haxsync.svg?branch=master)](https://travis-ci.org/Codarama/haxsync)
[![Coverage Status](https://coveralls.io/repos/Codarama/haxsync/badge.svg?branch=master&service=github)](https://coveralls.io/github/Codarama/haxsync?branch=master)
[![Static Code Analysis Status](https://scan.coverity.com/projects/6282/badge.svg)](https://scan.coverity.com/projects/codarama-haxsync)
[![Dependency Status](https://www.versioneye.com/user/projects/55eeb510211c6b0019001ae1/badge.svg?style=flat)](https://www.versioneye.com/user/projects/55eeb510211c6b0019001ae1)
[![License](http://img.shields.io/:license-GPL2-blue.svg)](http://www.gnu.org/licenses/gpl-2.0.html)

| Priority  | Status                  | Feature              | Percentage complete |
| :-------: | ----------------------- | -------------------- | ------------------- |
|1          |   :white_check_mark:    | Friends Photo Sync   |                 100%|
|2          |   :x:                   | Friend List Sync     |                   0%|
|3          |   :white_check_mark:    | Event Sync           |                 100%|
|4          |   :x:                   | Event Attendee Sync  |                   0%|
|5          |   :x:                   | Birthday Sync        |                   0%|



The project is currently **not available for download** at Google Play.

Q&A
---
**What are the next steps?**

1. :heavy_check_mark: Clean up project, set up build and test infrastructure
2. :heavy_multiplication_x: Test current functionality, fix any bugs that were introduced during the transition
3. :heavy_multiplication_x: Workaround issues with Facebook Android API
4. :heavy_multiplication_x: Release a new version via Google Play

**What ambitious future plans do you have?**

1. :heavy_check_mark: Upgrade APIs to latest versions
2. :heavy_multiplication_x: Remove dependencies on stale 3rd party libs
3. :heavy_multiplication_x: Sync from new social media sources

**Why did you change the package structure?**

I really appreciate that Mathias has left this project to the open source community. The licensing he has used has no explicit requirement to either stick with the original package names or change them. However, out of respect to his work, I'd like to make it possible to distinguish his original work and the modified version.

**How do you plan to workaround the issues of Facebook API not providing the required information**

There are really a number of ways. First step would be to see exactly which information is missing. The next one would be to provide an alternative way of retrieving it.

**Why do you need to hack Facebook's API?**

First off - I fully agree with Facebook's decision to improve the privacy of users by deprecating their APIs to automatically extract information about a given user's friends. However I believe that what this software is doing is not abusing this privacy improvement in any way. Everybody could manually download the profile picture of his friends in Facebook and manually place them as profile pictures in their phone book at any given moment. Automating this process would not jeopardize the privacy of anybody.

**Anything else?**

Write back and you will get the answers you need.

Contacts
---
Tihomir Mateev - lead developer
tihomir.mateev@gmail.com

Contributions to this project are welcome.
