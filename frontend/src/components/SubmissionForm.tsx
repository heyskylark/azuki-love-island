import { toast } from "react-toastify"
import { submitParticipant } from "../clients/MainClient"

interface Props {
    azukiId: string,
    twitterHandle: string,
    bio: string
    hobbies: string
    loadingSubmission: boolean
    setAzukiId: React.Dispatch<React.SetStateAction<string>>
    setTwitterHandle: React.Dispatch<React.SetStateAction<string>>
    setBio: React.Dispatch<React.SetStateAction<string>>
    setHobbies: React.Dispatch<React.SetStateAction<string>>
    setLoadingSubmission: React.Dispatch<React.SetStateAction<boolean>>
}

function SubmissionForm(props: Props) {
    const AZUKI_MAX_ID = 10000

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

        const participationRequest = {
            azukiId: props.azukiId,
            twitterHandle: props.twitterHandle,
            bio: bio,
            hobbies: hobbies
        }

        props.setLoadingSubmission(true);
        submitParticipant(participationRequest)
            .then(response => {
                const data = response.data;
                const toastMessage = `Welcome to Auzki Love Island: ${data.twitterHandle}`;

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

    function bioInput(e: React.ChangeEvent<HTMLTextAreaElement>): void {
        e.preventDefault();
        
        const eventInput = e.target.value;

        if (eventInput.length <= 200) {
            props.setBio(eventInput);
        }
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
                    onChange={(e) => props.setTwitterHandle(e.target.value)}
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
                    placeholder="Hobbies (comma seperated)"
                    value={props.hobbies}
                    onChange={(e) => props.setHobbies(e.target.value)}
                >
                </input>
            </label>

            <button className="uppercase w-full p-3 rounded-md text-white bg-azukired whitespace-nowrap" type="submit" disabled={props.loadingSubmission}>
                Submit
            </button>
        </form>
    );
}

export default SubmissionForm;
