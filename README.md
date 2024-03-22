# To-Do-List

### Info 
**Android Components:** Activity, Fragments, Broadcast Receiver  
**UI:** Data Binding + Constraints Layout  
**Architecture:** MVVM  
**Reactive Programming:** Kotlin Flows + Coroutines  
**Dependency Injection:** Hilt  
**HTTP Client:** Retrofit  
**Storage:** Preference DataStore  
**Worker:** Periodic  
**Unit Tests:** Mockito, Mock Server, Dagger Hilt Test support

### Overview
This project follows MVVM architecture with a Shared Viewmodel between screens. In Viewmodel, two states are maintained independent of each other (Shared flow and State flow). These states are responsible to update the other UI states which will update the UI elements visible on the screens and also will be used as inputs when communicating with the Repository. These other UI states are stored using SaveStateHandle which will recover them after process death.

Used Hilt to inject dependencies like DataStore in Repository, Worker, and Broadcast Receiver. It is also used to provide dependencies in Unit Tests cases.

Used Periodic Worker to send notifications regarding daily pending tasks that are not marked completed yet and used static Broadcast Receiver explicitly to mark those tasks completed when the user clicks on the notification.

