import { useState } from "react";

interface Props {
    registerTwitterHandle: (handle: string) => void
}

function TwitterHandleField(props: Props) {
    const [twitterHandle, setTwitterHandle] = useState<string>("");

    function startVote(e: React.FormEvent<HTMLFormElement> | undefined): void {
        e?.preventDefault();
        
        if (!twitterHandleInvalid()) {
            props.registerTwitterHandle(twitterHandle);
        }
    }

    function twitterHandleOnChange(e: React.ChangeEvent<HTMLInputElement>): void {
        e.preventDefault();

        const handle = e.target.value

        if (handle.length === 0 || new RegExp(/^[A-Za-z0-9_]{1,15}$/).test(handle)) {
            setTwitterHandle(handle);
        }
    }

    function twitterHandleInvalid(): boolean {
        if (twitterHandle.length === 0) {
            return true;
        }

        return !new RegExp(/^[A-Za-z0-9_]{1,15}$/).test(twitterHandle);
    }

    return (
        <div className="w-full flex flex-wrap justify-center lg:text-center">
            <div className="w-full lg:w-1/2">
                <form onSubmit={startVote}>
                    <button
                        className="transition-opacity ease-in-out delay-50 disabled:opacity-70 uppercase mb-8 w-full p-3 rounded-md text-white bg-azukired whitespace-nowrap hover:opacity-70"
                        type="submit"
                        disabled={twitterHandleInvalid()}
                    >
                        Start Vote
                    </button>
                    
                    <label>
                        <input
                            className="w-full mb-4 p-3 border-2 border-gray-100 focus:outline-none"
                            placeholder="@twitterHandle"
                            value={twitterHandle}
                            onChange={twitterHandleOnChange}
                        >
                        </input>
                    </label>
                </form>
            </div>
        </div>
    )
}

export default TwitterHandleField;
