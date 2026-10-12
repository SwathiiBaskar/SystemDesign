import { useEffect, useRef, useState } from "react";
import SuggestionList from "./SuggestionList";

interface SearchBoxProps {
    suggestions: string[];
    source: string;
    loading: boolean;
    onSearch: (prefix: string) => void;
}

function SearchBox({
    suggestions,
    source,
    loading,
    onSearch,
}: SearchBoxProps) {

    const [query, setQuery] = useState("");
    const [selectedIndex, setSelectedIndex] = useState(-1);

    const debounceTimer = useRef<number | null>(null);

    useEffect(() => {

        if (debounceTimer.current) {
            clearTimeout(debounceTimer.current);
        }

        if (query.trim().length === 0) {
            onSearch("");
            return;
        }

        debounceTimer.current = window.setTimeout(() => {
            onSearch(query);
        }, 300);

        return () => {
            if (debounceTimer.current) {
                clearTimeout(debounceTimer.current);
            }
        };

    }, [query]);



    const selectSuggestion = (suggestion: string) => {
        setQuery(suggestion);
        setSelectedIndex(-1);
    };


    const handleKeyDown = (
        event: React.KeyboardEvent<HTMLInputElement>
    ) => {

        if (event.key === "ArrowDown") {

            event.preventDefault();

            setSelectedIndex((previous) =>
                Math.min(previous + 1, suggestions.length - 1)
            );
        }


        if (event.key === "ArrowUp") {

            event.preventDefault();

            setSelectedIndex((previous) =>
                Math.max(previous - 1, 0)
            );
        }


        if (event.key === "Enter") {

            if (
                selectedIndex >= 0 &&
                suggestions[selectedIndex]
            ) {
                selectSuggestion(
                    suggestions[selectedIndex]
                );
            }
        }


        if (event.key === "Escape") {
            setSelectedIndex(-1);
        }
    };


    return (

        <div className="relative w-full">

            <div className="flex items-center rounded-2xl border border-slate-300 bg-white px-5 py-4 shadow-sm transition focus-within:border-slate-500 focus-within:shadow-md">

                {/* Search Icon */}

                <svg
                    className="mr-3 h-5 w-5 text-slate-400"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                >
                    <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="m21 21-4.35-4.35m2.35-5.15a7.5 7.5 0 1 1-15 0 7.5 7.5 0 0 1 15 0Z"
                    />
                </svg>


                <input
                    type="text"
                    value={query}
                    onChange={(event) => {
                        setQuery(event.target.value);
                        setSelectedIndex(-1);
                    }}
                    onKeyDown={handleKeyDown}
                    placeholder="Start typing to search..."
                    className="w-full bg-transparent text-lg text-slate-800 outline-none placeholder:text-slate-400"
                />


                {/* Loading */}

                {loading && (

                    <div className="h-5 w-5 animate-spin rounded-full border-2 border-slate-300 border-t-slate-700" />

                )}

            </div>


            {/* Source Badge */}

            {query && source && !loading && (

                <div className="mt-3 flex justify-end">

                    <span
                        className={`
                            rounded-full px-3 py-1 text-xs font-medium
                            ${
                                source === "redis"
                                    ? "bg-emerald-100 text-emerald-700"
                                    : "bg-amber-100 text-amber-700"
                            }
                        `}
                    >
                        {source === "redis"
                            ? "⚡ Redis Cache"
                            : "🌳 Trie Search"}
                    </span>

                </div>

            )}


            {/* Suggestions */}

            {query && (

                <SuggestionList
                    suggestions={suggestions}
                    selectedIndex={selectedIndex}
                    onSelect={selectSuggestion}
                />

            )}

        </div>
    );
}

export default SearchBox;