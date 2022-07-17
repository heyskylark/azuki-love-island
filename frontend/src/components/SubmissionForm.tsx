import React from "react"
import { toast } from "react-toastify"
import { submitParticipant } from "../clients/MainClient"
import { useLatestSeason } from "../context/SeasonContext"
import useImageUploader from "../hooks/useImageUpload";
import { SmallLoading } from "./Loading";

interface Props { 
    azukiId: string;
    twitterHandle: string;
    quote: string;
    bio: string;
    hobbies: string;
    participantCount: number;
    loadingSubmission: boolean;
    setParticipantCount: React.Dispatch<React.SetStateAction<number>>;
    setAzukiId: React.Dispatch<React.SetStateAction<string>>;
    setTwitterHandle: React.Dispatch<React.SetStateAction<string>>;
    setQuote: React.Dispatch<React.SetStateAction<string>>;
    setBio: React.Dispatch<React.SetStateAction<string>>;
    setHobbies: React.Dispatch<React.SetStateAction<string>>;
    setLoadingSubmission: React.Dispatch<React.SetStateAction<boolean>>;
}

function SubmissionForm(props: Props) {
    const AZUKI_MAX_ID = 10000;
    const MAX_QUOTE_LENGTH = 100;
    const DEFAULT_TRANSFORMS = "c_limit,w_2000";

    const latestSeasonContext = useLatestSeason();
    const {
        fileName,
        publicId,
        format,
        secureUrl,
        uploading,
        uploadImage,
        clearImage
    } = useImageUploader();

    function submitParticipantEvent(e: React.FormEvent<HTMLFormElement> | undefined): void {
        e?.preventDefault();

        let bio = undefined;
        if (props.bio.length > 0) {
            bio = props.bio;
        }

        let hobbies = undefined;
        if (props.hobbies.length > 0) {
            hobbies = props.hobbies;
        }

        let image;
        if (publicId) {
            image = {
                publicId: publicId,
                secureUrl: secureUrl,
                format: format
            }
        }

        const participationRequest = {
            azukiId: props.azukiId,
            twitterHandle: props.twitterHandle,
            quote: props.quote,
            bio: bio,
            hobbies: hobbies,
            image: image
        }

        props.setLoadingSubmission(true);
        submitParticipant(participationRequest)
            .then(response => {
                const data = response.data;
                const toastMessage = `Welcome to Auzki Love Island: ${data.twitterHandle}`;

                props.setAzukiId("");
                props.setTwitterHandle("");
                props.setQuote("");
                props.setBio("");
                props.setHobbies("");
                clearImage();

                props.setParticipantCount(props.participantCount + 1);
                toast.success(toastMessage);
            })
            .catch(err => {
                const data = err.response.data;

                toast.error(data.message);
            })
            .finally(() => {
                props.setLoadingSubmission(false);
            });
    }

    function twitterHandleOnChange(e: React.ChangeEvent<HTMLInputElement>): void {
        e.preventDefault();

        const handle = e.target.value

        if (handle.length === 0 || new RegExp(/^[A-Za-z0-9_]{1,15}$/).test(handle)) {
            props.setTwitterHandle(handle);
        }
    }

    function azukiIdInput(e: React.ChangeEvent<HTMLInputElement>): void {
        e.preventDefault();

        const previous = props.azukiId;
		const eventInput = e.target.value.replace(/\D/g, '');
        const value = parseInt(eventInput);
        
        if (eventInput.length === 0) {
            props.setAzukiId("");
        } else if (eventInput.length === 0 || (value >= 0 && value < AZUKI_MAX_ID)) {
            props.setAzukiId(eventInput);
        } else {
            props.setAzukiId(previous);
        }
    }

    function quoteInput(e: React.ChangeEvent<HTMLInputElement>): void {
        e.preventDefault();

        const quote = e.target.value;

        if (quote.length === 0 || quote.length <= MAX_QUOTE_LENGTH) {
            props.setQuote(quote);
        }
    }

    function bioInput(e: React.ChangeEvent<HTMLTextAreaElement>): void {
        e.preventDefault();
        
        const eventInput = e.target.value;

        if (eventInput.length <= 200) {
            props.setBio(eventInput);
        }
    }

    function submissionsDisabled(): boolean {
        // Quote exists, twitter handle exists, azuki id exists, image is not uploading
        const quoteBlank = props.quote.length === 0;
        const twitterHandleBlank = props.twitterHandle.length === 0;
        const azukiIdBlank = props.azukiId.length === 0;

        const submissionsActive = latestSeasonContext?.latestSeason?.submissionActive;
        const invalidFormState = quoteBlank || twitterHandleBlank || azukiIdBlank || uploading;

        return !submissionsActive || props.loadingSubmission || invalidFormState
    }

    return (
        <form onSubmit={submitParticipantEvent}>
            <label>
                <input
                    className="w-full mb-4 p-3 border-2 border-gray-100 focus:outline-none"
                    type="text"
                    pattern="\d*"
                    placeholder="Azuki id..."
                    value={props.azukiId}
                    onChange={(e) => azukiIdInput(e)}
                >
                </input>
            </label>
            <label>
                {/* <img className="w-7 h-7 mr-2" alt="Twitter Logo" src="/images/twitter-logo.png" /> */}
                <input
                    className="w-full mb-4 p-3 border-2 border-gray-100 focus:outline-none"
                    placeholder="Twitter handle..."
                    value={props.twitterHandle}
                    onChange={(e) => twitterHandleOnChange(e)}
                >
                </input>
            </label>
            <label>
                <input
                    className="w-full mb-4 p-3 border-2 border-gray-100 focus:outline-none"
                    placeholder="Quote (100 characters)..."
                    value={props.quote}
                    onChange={(e) => quoteInput(e)}
                >
                </input>
            </label>
            <label>
                <textarea
                    className="w-full mb-4 p-3 h-48 border-2 border-gray-100 focus:outline-none"
                    placeholder="Tell us about yourself (200 characters max)"
                    value={props.bio}
                    onChange={(e) => bioInput(e)}
                >
                </textarea>
            </label>
            <label>
                <input
                    className="w-full mb-6 p-3 border-2 border-gray-100 focus:outline-none"
                    placeholder="Hobbies (5 max, comma separated)"
                    value={props.hobbies}
                    onChange={(e) => props.setHobbies(e.target.value)}
                >
                </input>
            </label>

            <label>
                <p className="mb-2 underline font-mono text-gray-800 lg:text-sm text-xs lg:leading-6 leading-4">{"Fan Art: Express Yourself! (Max 10 MB)"}</p>

                <div className="flex">
                    <input
                        className="mb-6 p-3 pl-0 focus:outline-none font-mono text-gray-800 lg:text-sm text-xs lg:leading-6 leading-4"
                        type="file"
                        name={fileName}
                        accept="image/*"
                        disabled={!latestSeasonContext?.latestSeason?.submissionActive}
                        onChange={(e) => uploadImage(e.target.files, DEFAULT_TRANSFORMS)}
                    />
                    { uploading ? <SmallLoading /> : <></> }
                </div>
            </label>

            <button
                className="transition-opacity ease-in-out delay-50 disabled:opacity-70 uppercase mb-8 lg:mb-0 w-full p-3 rounded-md text-white bg-azukired whitespace-nowrap hover:opacity-70"
                type="submit"
                disabled={submissionsDisabled()}
            >
                Submit
            </button>
        </form>
    );
}

export default SubmissionForm;
