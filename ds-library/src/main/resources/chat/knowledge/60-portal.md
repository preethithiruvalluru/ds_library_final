category: portal
topic: This website

## What can I do on this website?
keywords: website, portal, this website, this site, features, what is this, sections, how to use, how to use the website, how to use the portal, guide, overview, tour
questions: what is this portal | what can this website do | how do i use this site | what are the sections
starter: yes

This is the **Data Science Digital Library**, a study portal. Open the **menu** (the button at the top left) to visit:

- **Modules**: course modules with syllabus and study-material **PDFs** you can read in a book-style viewer
- **Running Notes**: write, search and tag your class notes
- **Lab Experiments**: aim, code, output and result for each experiment
- **Tools**: data science tools with an official website and a **YouTube playlist** for each
- **Resources**: PDFs, links, tutorials and videos
- **Code Editor**: write and run code (Python and JavaScript run right in your browser)

The round photo at the top right opens your **profile**.

## How do I open or close the menu?
keywords: menu, sidebar, side bar, navigation, open menu, close menu, menu button, hamburger, where is the menu, hide menu
questions: where is the menu | how to open the sidebar | menu is not showing | how to close the menu

Use the **three-line button at the top left** (next to the logo) to open or close the menu. The menu starts **closed** every time the page loads. The small numbers beside menu items show how many modules, notes, experiments, tools and resources there are.

## How do I read the syllabus or study material PDFs?
keywords: syllabus, study material, pdf, read pdf, open pdf, pdf viewer, book viewer, page flip, download pdf, notes pdf, modules
questions: where is the syllabus | how to open a pdf | how to read study material | where can i download study material

Open **Modules** from the menu. Each module card shows its **Syllabus** and **Study material** PDFs. Click a PDF name to open the **book-style reader**: flip pages with the arrows (or the left and right arrow keys), zoom in and out, go full screen, or download it. Press **Esc** or the red X to close.

If a module has no PDF yet, someone needs to upload it (see "How do I add a module?").

## How do I add or edit a module and upload PDFs?
keywords: add module, new module, edit module, delete module, upload pdf, upload syllabus, upload study material, create module
questions: how to add a module | how to upload a syllabus | how to upload study material | how to delete a module

1. Menu, then **Modules**, then **Add Module**. Fill in the module number, name, subject and description.
2. On the module card use **Upload syllabus** or **Add material** to attach PDFs (up to 25 MB each).
3. The pencil edits the module and the bin deletes it (with its PDFs).

There is **no login**, so anyone using the portal can add, edit or delete content. Be careful before deleting.

## How do I write and search notes?
keywords: notes, running notes, new note, write notes, add note, search notes, tags, filter notes, save notes
questions: how to add a note | how to search my notes | where do i write notes | how to tag notes

Menu, then **Running Notes**, then **New Note**. Give it a title, subject, optional module, the content and tags (separated by commas). Use the search box to search titles, content and tags, and the drop-downs to filter by module, subject or tag. Click a note title to read it in full. Notes are saved in the database, so they are still there after a refresh or restart.

## How do I add a lab experiment?
keywords: lab experiment, experiments, add experiment, lab record, experiment, source code, output image, aim, procedure, result, github repository
questions: how to add an experiment | where to put lab programs | how to upload output screenshot | how to link github repo

Menu, then **Lab Experiments**, then **Add Experiment**. Fill in the number, title, aim, requirements, procedure, **source code** and language, the output (text and/or an **image**, PNG/JPEG/GIF/WebP up to 5 MB), the result, the related module and an optional **GitHub repository link**.

Open an experiment with **View** to see highlighted code, a **Copy code** button, the output, and live details of the linked GitHub repository (stars, description, last push).

## How do I run code in the Code Editor, and which languages work?
keywords: code editor, run code, editor, run python, run java, run javascript, run c, compile, output console, code runner, save code, load code, monaco
questions: how to run code | which languages can i run | why is the run button disabled | how to save my code | java not running

Menu, then **Code Editor**. Pick a language, write code, type any input in the **Input** box (one value per line) and press **Run** (or Ctrl+Enter).

- **Python** and **JavaScript** run **inside your browser** (10 second limit, no network). The first Python run downloads the Python engine (about 10 MB), so it needs internet once.
- **Java, C and C++** run only when the separate **code runner** (needs Docker) is started on the computer hosting the portal. If the **Run** button is disabled for them, it is not set up there.
- **R and SQL** can be written and **saved**, but cannot be run yet.

Use **Save** to keep your code and **Load** to bring it back.

## Where can I find tutorials and YouTube playlists for a tool?
keywords: tools page, tool, tutorial, youtube playlist, learn tool, playlist, official website, tutorials, videos
questions: where are the youtube playlists | how to learn numpy pandas | where to find tutorials

Menu, then **Tools**. Each tool card has an **Official website** button and a red **YouTube playlist** button with a course chosen for that tool. You can change a playlist with the pencil icon, or add new tools. For more links, videos and PDFs, open **Resources**.

## How do I add resources, links or videos?
keywords: resources, add resource, resource, link, links, youtube video, watch video, documentation, upload resource, tutorial link
questions: how to add a link | how to add a youtube video | where to find study resources

Menu, then **Resources**, then **Add Resource**. Choose the type: PDF, website link, documentation, tutorial, **YouTube video** or learning material. YouTube links get a **Watch here** button that plays the video inside the page. Use the search box and filters (type, subject, module, tag) to find things.

## How do I update my profile, photo or resume?
keywords: profile, my profile, profile photo, photo, resume, edit profile, id card, student id, skills, linkedin, github profile
questions: how to change my photo | how to upload my resume | how to edit my details | where is my profile

Click the **round photo at the top right**. On the profile page press **Edit** to enter your name, roll number, department, section, college, mentor, email, GitHub and LinkedIn links and skills, and to upload a **photo** and a **resume (PDF)**. Your details appear on the ID card. If you add your GitHub profile link, your public repositories are listed below it.

## Do I need to log in? Who can change things?
keywords: login, log in, password, account, sign in, who can edit, permissions, security, delete, no login
questions: is there a login | how to log in | who can delete my notes | is my data safe

There is **no login** in this portal. Anyone who can open the website can read, add, edit and delete content, so only share the link with people you trust and take care before deleting anything.

## How does this chat bot work? Does it use AI or the internet?
keywords: chatbot, chat bot, this bot, study bot, how does the bot work, ai, artificial intelligence, offline, internet, api key, answers, how you work
questions: are you ai | do you use the internet | how do you answer | how does the chatbot work | are you chatgpt

I am a **simple offline helper**, not an AI service. I compare the **words in your question** with a ready-made list of answers and reply with the best match. Nothing you type is sent anywhere outside this portal, and no API key is needed.

That means I only know what is written in my answer list. If I can't find your question, try different words, or ask a teacher. The portal owner can add more answers (see the `chat/knowledge` files).
