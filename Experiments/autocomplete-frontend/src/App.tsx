import { useEffect, useRef, useState } from "react";
import "./App.css";

interface AutocompleteResponse {
  prefix: string;
  suggestions: string[];
  source: string;
}

function App() {
  const [query, setQuery] = useState("");
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const [source, setSource] = useState("");
  const [selectedIndex, setSelectedIndex] = useState(-1);

  const debounceTimer = useRef<number | null>(null);

  useEffect(() => {
    if (debounceTimer.current) {
      clearTimeout(debounceTimer.current);
    }

    if (query.trim().length === 0) {
      setSuggestions([]);
      setSource("");
      return;
    }

    debounceTimer.current = window.setTimeout(() => {
      fetchSuggestions(query);
    }, 300);

    return () => {
      if (debounceTimer.current) {
        clearTimeout(debounceTimer.current);
      }
    };
  }, [query]);

  const fetchSuggestions = async (prefix: string) => {
    try {
      setLoading(true);

      const response = await fetch(
        `http://localhost:8080/api/autocomplete?prefix=${encodeURIComponent(
          prefix
        )}&k=5`
      );

      if (!response.ok) {
        throw new Error("Failed to fetch suggestions");
      }

      const data: AutocompleteResponse = await response.json();

      setSuggestions(data.suggestions || []);
      setSource(data.source || "");
      setSelectedIndex(-1);
    } catch (error) {
      console.error("Error fetching suggestions:", error);
      setSuggestions([]);
      setSource("");
    } finally {
      setLoading(false);
    }
  };

  const selectSuggestion = (suggestion: string) => {
    setQuery(suggestion);
    setSuggestions([]);
    setSource("");
  };

  const handleKeyDown = (
    event: React.KeyboardEvent<HTMLInputElement>
  ) => {
    if (suggestions.length === 0) return;

    if (event.key === "ArrowDown") {
      event.preventDefault();

      setSelectedIndex((prev) =>
        prev < suggestions.length - 1 ? prev + 1 : 0
      );
    }

    if (event.key === "ArrowUp") {
      event.preventDefault();

      setSelectedIndex((prev) =>
        prev > 0 ? prev - 1 : suggestions.length - 1
      );
    }

    if (event.key === "Enter" && selectedIndex >= 0) {
      selectSuggestion(suggestions[selectedIndex]);
    }

    if (event.key === "Escape") {
      setSuggestions([]);
      setSelectedIndex(-1);
    }
  };

  const clearSearch = () => {
    setQuery("");
    setSuggestions([]);
    setSource("");
    setSelectedIndex(-1);
  };

  return (
    <div className="app">
      <header className="navbar">
        <div className="brand">
          <div className="brand-icon">A</div>
          <span>AutoFind</span>
        </div>

        <div className="nav-status">
          <span className="status-dot"></span>
          Autocomplete Engine
        </div>
      </header>

      <main className="main-content">
        <section className="hero">
          <p className="tag">PREFIX-BASED SEARCH</p>

          <h1>
            Autocomplete
          </h1>

          <p className="subtitle">
            Fast autocomplete powered by Trie-based prefix matching
            and Redis caching.
          </p>
        </section>

        <section className="search-section">
          <div className="search-wrapper">
            <div className="search-box">
              <span className="search-icon">⌕</span>

              <input
                type="text"
                value={query}
                placeholder="Start typing something..."
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={handleKeyDown}
                autoFocus
              />

              {loading && (
                <div className="loader"></div>
              )}

              {!loading && query && (
                <button
                  className="clear-button"
                  onClick={clearSearch}
                >
                  ×
                </button>
              )}
            </div>

            {suggestions.length > 0 && (
              <div className="suggestions-container">
                <div className="suggestion-header">
                  <span>Suggestions</span>

                  {source && (
                    <span
                      className={
                        source === "redis"
                          ? "source redis"
                          : "source trie"
                      }
                    >
                      {source === "redis"
                        ? "⚡ Redis Cache"
                        : "⌘ Trie Search"}
                    </span>
                  )}
                </div>

                {suggestions.map((suggestion, index) => (
                  <button
                    key={index}
                    className={
                      selectedIndex === index
                        ? "suggestion active"
                        : "suggestion"
                    }
                    onClick={() =>
                      selectSuggestion(suggestion)
                    }
                    onMouseEnter={() =>
                      setSelectedIndex(index)
                    }
                  >
                    <span className="suggestion-icon">⌕</span>

                    <span>{suggestion}</span>

                    <span className="arrow">↗</span>
                  </button>
                ))}
              </div>
            )}

            {query.trim() !== "" &&
              !loading &&
              suggestions.length === 0 && (
                <div className="no-results">
                  No suggestions found for "{query}"
                </div>
              )}
          </div>
        </section>

        <section className="info-section">
          <div className="info-card">
            <div className="info-icon">⌘</div>
            <div>
              <h3>Trie Search</h3>
              <p>
                Efficiently finds words that match the typed prefix.
              </p>
            </div>
          </div>

          <div className="info-card">
            <div className="info-icon">⚡</div>
            <div>
              <h3>Redis Cache</h3>
              <p>
                Returns previously computed suggestions instantly.
              </p>
            </div>
          </div>

          <div className="info-card">
            <div className="info-icon">★</div>
            <div>
              <h3>Top-K Results</h3>
              <p>
                Suggestions are ranked using search frequency.
              </p>
            </div>
          </div>
        </section>
      </main>

      <footer>
        <span>Autocomplete Search System</span>
        <span>Spring Boot · Trie · Redis</span>
      </footer>
    </div>
  );
}

export default App;