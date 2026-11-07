# Brewkery ☕🥐

**Brewkery** is a modern, artisanal coffee & oven bakes ordering Android application built with **Jetpack Compose** and **Material Design 3**. It offers an intuitive online coffee ordering experience inspired by modern café apps, featuring real-time menu browsing, dynamic item customization, live order tracking, and an expandable order history profile drawer.

---

## 🎨 Theme & UI Design
- **Theme Palette**:
  - **Primary**: `#362419` (Velvety Dark Chocolate Mocha)
  - **Secondary**: `#E0A96D` (Golden Warm Caramel)
  - **Background**: `#F4EAE1` (Warm Oat Milk Neutral)
- **Typography & Styling**: Elegant Serif branding ("Brewkery") paired with clean Material 3 typography.
- **Micro-Animations**: Custom spring-bounce click feedback on buttons and cards, smooth expandable cards, horizontal pager dot indicators, and celebratory order completion animations.

---

## 🛠️ Architecture & Tech Stack

- **Language**: 100% Kotlin
- **Architecture**: MVVM + Clean Architecture with Repository Pattern
- **UI Framework**: Jetpack Compose with Material 3 components
- **Navigation**: Jetpack Navigation Compose
- **Async & Reactive Data**: Kotlin Coroutines & Flow with `collectAsStateWithLifecycle()`
- **Networking**: Retrofit 2 + Gson Converter + OkHttp Logging Interceptor
- **Local Persistence**: Room Database (saving cart items & order history locally)
- **Image Loading**: Coil Compose
- **Testing**: JUnit 4 & Kotlinx Coroutines Test (Unit test coverage for models, cart calculations, search filtering, and order status transitions)

---

## 📱 Screen Flows

### 1. Home / Menu Screen
- Top Bar with stylized *Brewkery* branding and Profile button.
- Store Announcement Banner or Active Orders Pager Banner (with dot indicators for multiple active orders).
- Search bar with live multi-field filtering (title, tagline, description).
- Category filter chips (*All Items*, *Hot Coffee*, *Cold Brews*, *Artisan Bakery*).
- 2-column menu grid displaying full item titles across up to 2 lines without truncation.

### 2. Item Detail & Customization Screen
- High-res hero image, rating, calories, prep time, description, and ingredient tags.
- Dynamic customization controls:
  - **Size selection** (Tall, Grande, Venti) with live price adjustment.
  - **Milk selection** (Oat Milk, Almond Milk, Whole Milk) with price add-ons.
  - **Sweetness / Preparation** level options.
- Quantity selector (+/-) recalculating unit price, subtotal, delivery charges ($2.50), tax (8%), and total in real-time.
- "Add to Order" action button.

### 3. Cart Screen
- Detailed list of cart items showing selected customizations.
- Quantity increment/decrement and swipe-to-dismiss deletion.
- Itemized pricing summary (Subtotal, Delivery Fee, Tax 8.0%, Grand Total).
- "Clear All" action and "Place Order Now" checkout button.

### 4. Order Status Screen
- Order placement confirmation with ticket generation (`BK-{itemCount}{4-digit-ID}`).
- One-time spring-bounce checkmark celebration animation.
- Interactive Order Progress Steps Timeline (*Received* → *Brewing* → *On The Way* → *Delivered*).
- Countdown tracker updating status from *PREPARING* to *OUT FOR DELIVERY* and *DELIVERED*.
- Delivered orders remain visible on the active banner for 2 minutes before hiding, and persist in the **Profile Order History**.

### 5. Profile & Order History Side Sheet
- Right-side sliding panel accessible from the top bar Profile button.
- User profile header with avatar, email, and rewards tier status.
- Saved order history list sorted chronologically.
- Expandable order cards (`animateContentSize`) revealing complete billing breakdowns (subtotal, delivery, tax, grand total).

---

## 🌐 API Specification

- **Catalog API**: `https://raw.githubusercontent.com/VivekShah138/Brewkery/main/data.json`
- **Item Detail API**: `https://raw.githubusercontent.com/VivekShah138/Brewkery/main/api/items/{id}.json` (where `{id}` is 1, 2, 3, 4, 5, 6, etc.)

---

## 🤖 AI Development & Prompt Log

### **AI Assistant**: `Gemini 3.6 Flash (Inbuilt in Android Studio)`

The application development was driven and refined through iterative prompting with **Gemini 3.6 Flash**:

```markdown
### Prompt 1: Initial Architecture & Implementation Plan
"create implement plan for the "Brewkery" where this app is Coffee app (just like the Starbucks where user can order the coffee online using the app )first understand the all the requirement and need of this app then make implement plan.

theme/ui - give suggestion of the relevant theme (Deep Mocha & Caramel Primary: #362419 (Velvety Dark Chocolate Mocha) Secondary: #E0A96D (Golden Warm Caramel) Background: #F4EAE1 (Warm Oat Milk Neutral))where match the coffee and cosy and relax vibe , minimalist ui (give deferent option of the color theme and ui to chose between from the implement plan), use the appropriate font accoding tot the app , strictly use of the material 3 component of compose ,animation for every button and card for clicked and hover, all icons from the compose icon library (no use of the emoji(only use the emoji when it come form the api data) only use the icon ) , use Material 3 Compose Loading Indicator for the every page to load for .5 sec for every screen , no need of the navigation bar in bottom

tech stake - "navigation 3 , material 3 ,retrofit , gson , room for the save histoy of the order and cart item that in the avible before the app close ,coroutine api data from the {https://raw.githubusercontent.com/VivekShah138/Brewkery/main/data.json details of the coffee and other} ,{https://raw.githubusercontent.com/VivekShah138/Brewkery/main/api/items/{1,2,3,4,5,6}.json specific data of the that item} "

architecture - MVVM , clean architecture , use the Enum state for (success ,fail ,loading for every state and show the appropriate ui according to it ),loosely cupel (so it can one thing fail/remove or new thing add don't crash whole app), code maintainable , scalable , and readable to developer so it can maintain and can understand , every thing cover in to try catechin all the error and all the activity all show in the logs , if user has no internet then the show the no internet , and when api call fail for fetch data show the "something went wrong" screen and user cant order or cant do any thing

Screen 1 — Home / Menu: show the categories row and the items list (thumbnail, badge, title, rating, base price). user can search the item using the search bar (show the all the item hear with base price in card and it derails about the item) (use the "Brewkery" italic bold spacing letter 1 in top center of the top bar and in the right side user profile logo (using the martial 3 icons). Includes the store announcement banner (not order state show the time take for the reach order and delivery charges [no countdown]) (and active order tracking once placed in that banner show that how much time remain[show random delivery time range form 5 min to 15 min this shoude be consistent and after order to means if for example user open the app and show the 12 min for example then it shoude be the same when the user place the order and then show the countdown + [if delivery time is still remain show to delivery time + preparing time and there show the if order in the preparing or out for the order] [only min no seconds] ,if multiple order then show it side by side and user can side it ,show cart button using the fab compose (bottom bar) that redirect to the cart screen and show the top show much item in the that cart.

Screen 2 — Item Detail: tap an item → hero image, info, ingredients, and dynamic customizations (show every thing about the that item according to api) (user can customize it from deferent option available for that specific item and order that but total price change according to the size and ingredients change and it dynamic change, in the total include the tax and the delivery charges and show the total) (user can directly order form hear to with item number can be increase and can be ) (sizes, milk_options, sugar_levels) with price recalculation.show , user can order multiple same item form hear means can increase the item count and price change according do it , also user can order same item but deferent addons or the size of the coffee to if he want it show sperate item in the cart with item

Screen 3 — Cart: tap cart → items list with quantities, subtotal + $2.50 delivery + 8% tax calculation. (show the all the items in the cart and show the all the tax + customization addons + delivery in appropriate order) ( main button where user can place the order and it redirect to the Order Status page ) , with slide can user can remove that item form the cart and give clear every thing item for the cart.

Screen 4 — Order Status: place order → clear cart, generate ticket ID (BK-{item number}{4 digit number start from the 1001}), and show PREPARING status (after that time of preparing show the out for delivery and show random delivery time range form 5 min to 15 min ,show both countdown of the remaining time) show the animation of the successfully order in only the order status when it success it only one time, button for the go back to main menu.

understand the requirement see and what can be improve and what can be done extra and make app more user friendly and more easy to use"

### Prompt 2: Main Menu Layout & Item Detail API Refinement
"https://raw.githubusercontent.com/VivekShah138/Brewkery/main/api/items/{id}.json id is from 1 2 3 4 5 6 that ide is alred avible in the https://raw.githubusercontent.com/VivekShah138/Brewkery/main/data.json that id is details aboud the that item like id: 1 or something , and remove the discerption of the item in the main screen only title of the item and it shoud be show full use second line if need"

### Prompt 3: Touch Event Interception Bugfix
"something is off in the menu card it not working as indented , user clicked on the card of the item but it not opening any thing see what's is issue why it not working https://raw.githubusercontent.com/VivekShah138/Brewkery/main/api/items/{id}.json see what is issue and fix it according to the implemt plan"

### Prompt 4: Active Order Pager Dots, Instant API Loading, Profile Order History & UI Enhancements
"1).in the slide for the mutliple order process show the thire other order details ccar by dot slider so user know thire is other order details , 2). why it api call take time see why it taking time and loading time is too much if loading time dont need like if the api data come fast then the do not use the lading screen , 3). save the order histy and show in the profile (if usr cclike on to the profile then it opern the right side slider and thire section for the histy of the order show the card of the order (otder id , orderx amount , order title , order time , and if clcike on the card expend then show the billing ammount ))4)impove other factore like titile and ui make it more asthetic ana cleae an d add more intractiev aniamtiona and other animation forr the order complet and other fthing"
```

---

## 🧪 Unit Testing

Run unit tests via Gradle:
```bash
./gradlew test
```
- **18 Unit Tests Passing**:
  - `CartCalculationTest`: Unit price, total price, 8% tax, delivery charges, and customization hash uniqueness.
  - `HomeFilterTest`: Category chip filter and multi-field keyword search logic.
  - `OrderStatusTest`: `PREPARING` → `OUT FOR DELIVERY` → `DELIVERED` status transitions and 2-minute slider banner removal.
  - `CoffeeModelsTest`: Gson serialization/deserialization of catalog API responses and order items summary JSON.

---

## 📄 License
Designed & Developed for **Brewkery** Coffee App.
