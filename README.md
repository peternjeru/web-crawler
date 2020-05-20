## Extremely simplified parser for searching for jobs (mainly). 

Hobby project to help crawl for jobs on the Internet at a much faster rate

Project is set up as a simple maven Spring Boot application. To run, simply configure `application.yml` with your custom database settings, set a path to any folder in your local machine to save current status via the `path` variable in the class `ke.co.proxyapi.crawler.Runner`, then execute.

To implement custom search, check the class `ke.co.proxyapi.crawler.Runner`. There are multiple lists examples showing how to implement a simple custom search. Each inner list represents a search term that one would put in a browser e.g. the list `'software developer', 'job|vacancy|position|opportunity', 'kenya', '2020'` means that if the above terms were used by a human searching, the above list would result in the following search phrases:

- software developer job kenya 2020
- software developer vacancy kenya 2020
- software developer position kenya 2020
- software developer opportunity kenya 2020

Similarly, if the list `'senior software engineer', 'job|vacancy|position|opportunity', 'kenya'` were used, the resulting search phrases would be one of the following

- senior software engineer job kenya
- senior software engineer vacancy kenya
- senior software engineer position kenya
- senior software engineer opportunity kenya

You can use the `|` character to *OR* multiple similar terms e.g. as shown by the terms *job* and *vacancy* in the above examples

Then, to view a list of the `n` most recent entries fetched, just enter the command `list n` in the terminal and press enter. The system will fetch the latest `n` entries and list them on the terminal. Use discretion in checking, as not all will point to valid job adverts (assuming that's what you are using it for). If `n` is not given, the last 100 entries will be returned instead.

Database used is PostgreSQL, but if you are adept, you can change to whatever you have installed locally

Also, be patient, crawling takes time, so give it at least 5 - 10 minutes to begin viewing good results. The longer it runs, the better. Then, warning, it might be a CPU hog :-)
