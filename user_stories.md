## Feature: User Stories

### US-01: Toggle Content Type

- **As a user**,
  I want to switch between a "Movies" tab and a "TV Shows" tab,
  So that I can filter the feed strictly to the media type I want to browse.
- **Acceptance Criteria**:
  - A top app bar with segmented tabs or tabs for "Movies" and "TV Shows" is displayed.
  - Selecting "Movies" triggers `GET /api/movies?page=1` and displays the movie grid.
  - Selecting "TV Shows" triggers `GET /api/tv-shows?page=1` and displays the TV show grid.
  - Switching between tabs preserves the existing feed list and scroll position in-memory without firing duplicate network requests for cached data.

### US-02: Browse Media Grid

- **As a user**,
  I want to view a responsive vertical grid of media posters,
  So that I can scan titles visually and select one to explore.
- **Acceptance Criteria**:
  - Feed items render in a 2-column or 3-column `LazyVerticalGrid` based on screen width.
  - Each item card displays:
    - Poster image (loaded asynchronously).
    - Title.
    - Release year and rating badge (if present in API response).
  - Shimmer placeholder is displayed while images are loading.
  - Fallback placeholder image appears if the image URL fails to load.
  - Tapping an item navigates to the Media Detail screen using the item's `slug`.

### US-03: Infinite Pagination

- **As a user**,
  I want the feed to load more titles automatically as I scroll down,
  So that I have an uninterrupted browsing experience without manual page navigation.
- **Acceptance Criteria**:
  - Next page query (`page = currentPage + 1`) automatically triggers when the user scrolls within 3-4 items from the bottom.
  - A bottom circular progress indicator is shown while fetching the next page.
  - If the last page is reached (`has_more: false` or empty list), pagination stops and no further API requests are dispatched.
  - Network pagination failure shows a bottom inline banner with a "Retry" button.

---

## Feature: Search & Discovery

### US-04: Search Titles

- **As a user**,
  I want to search for titles via a keyword search bar,
  So that I can quickly locate specific movies or TV shows.
- **Acceptance Criteria**:
  - A persistent search input with a clear ("X") button is provided.
  - API call to `GET /api/search?keyword={query}&page=1` triggers after a 400ms debounce once the user stops typing.
  - Search results display in a list or grid format.
  - Each card displays a clear badge indicating `[Movie]` or `[TV Show]`.
  - Tapping the clear ("X") button clears the input and restores the default/empty search state.

### US-05: Handle Search Feedback & Edge Cases

- **As a user**,
  I want to see descriptive feedback when a search returns no items or fails,
  So that I understand whether my query yielded zero results or the system encountered an error.
- **Acceptance Criteria**:
  - Empty results return an empty-state illustration with: `"No results found for '{query}'"`.
  - Network timeout or HTTP 5xx error shows an error state with a dedicated "Retry" button.
  - White space only queries (e.g., `"   "`) do not trigger API requests.

---

## Feature: Media Details & Episodes

### US-06: View Media Overview

- **As a user**,
  I want to view full metadata for a selected title,
  So that I can evaluate plot details, ratings, and download availability.
- **Acceptance Criteria**:
  - Route triggers `GET /api/movies/{slug}` or `GET /api/tv-shows/{slug}`.
  - Detail screen renders:
    - Backdrop / Poster image.
    - Full title.
    - Release year, runtime, and content/user ratings.
    - Genre chips.
    - Plot summary text.
  - Centered loading spinner is shown during initial load.
  - If the request fails, an error view with a "Retry" button replaces the screen content.

### US-07: TV Show Season & Episode Selection

- **As a user**,
  I want to choose a specific season and episode for a TV show,
  So that I can access the exact download links associated with that episode.
- **Acceptance Criteria**:
  - If the media type is `TV Show`, a season selector (segmented tabs or dropdown) is rendered.
  - Selecting a season updates the list of available episodes for that season.
  - Default selection targets Season 1, Episode 1 on screen load.
  - Selecting an episode updates the Download Link Action Hub to display links matching that specific episode.

---

## Feature: Download Action Hub

### US-08: View External Download Links

- **As a user**,
  I want to see all available download sources grouped by provider and quality,
  So that I can select my preferred download medium.
- **Acceptance Criteria**:
  - Download options render as distinct action cards with:
    - Provider label (e.g., Telegram, MegaUp, Direct).
    - Resolution/Quality badge (e.g., 720p, 1080p).
    - File size (if provided by the payload).
  - If no download links exist for the title or selected episode, show: `"No download links currently available"`.

### US-09: Launch External Download Link

- **As a user**,
  I want to tap a download button to open it immediately in the appropriate app or browser,
  So that I can begin downloading without manual setup.
- **Acceptance Criteria**:
  - Tapping a Telegram link launches an Android `ACTION_VIEW` intent targeted at the Telegram package (`org.telegram.messenger` / Telegram clients).
  - If Telegram is not installed, it falls back to launching the Telegram Web link in the default browser.
  - Tapping HTTP/Direct/MegaUp links opens via Android Custom Tabs or default system browser.

### US-10: Copy Download Link

- **As a user**,
  I want to copy the direct link URL to the clipboard with one tap,
  So that I can easily paste it into external download tools (such as 1DM or ADM).
- **Acceptance Criteria**:
  - Each download option card contains a secondary "Copy" icon button.
  - Tapping the icon writes the raw link URL to Android's `ClipboardManager`.
  - A brief Toast or Snackbar feedback displays: `"Link copied to clipboard"`.
