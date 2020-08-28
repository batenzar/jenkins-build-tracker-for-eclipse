# jenkins-build-tracker-for-eclipse [Implementing]
Jenkins Build Tracker plugin for Eclipse  
  
## How to Install
1. Export jar as deployable plug-ins and fragments
2. Place the jar file in <eclipse>/dropins
3. Restart Eclipse

## Sample URL
https://{{server}}/job/{{job_folder}}/job/{{job_name}}/lastBuild/api/json?tree=fullDisplayName,result  

Ref: https://stackoverflow.com/questions/26236206/jenkins-remote-api-is-it-possible-to-retrieve-the-complete-job-tree-using-the
