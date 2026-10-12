interface SuggestionListProps {
    suggestions: string[];
    selectedIndex: number;
    onSelect: (suggestion: string) => void;
}

function SuggestionList({
    suggestions,
    selectedIndex,
    onSelect,
}: SuggestionListProps) {

    if (suggestions.length === 0) {
        return null;
    }

    return (
        <div className="absolute left-0 right-0 top-full mt-3 overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xl">

            {suggestions.map((suggestion, index) => (
                <button
                    key={suggestion}
                    onMouseDown={() => onSelect(suggestion)}
                    className={`
                        flex w-full items-center gap-3 px-5 py-4 text-left
                        transition
                        ${
                            selectedIndex === index
                                ? "bg-slate-100"
                                : "hover:bg-slate-50"
                        }
                    `}
                >

                    <span className="flex h-8 w-8 items-center justify-center rounded-full bg-slate-100 text-sm">
                        ↗
                    </span>

                    <span className="text-slate-700">
                        {suggestion}
                    </span>

                </button>
            ))}

        </div>
    );
}

export default SuggestionList;