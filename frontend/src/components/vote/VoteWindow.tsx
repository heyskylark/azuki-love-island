import ParticipantResponse from "../../models/api/ParticipantResponse";
import serialToImageNameJson from "../../models/image/ImageToUUIDMapping";
import { getBackgroundColor, textColor } from "../../util/ColorUtil";

interface Props {
    participant1: ParticipantResponse;
    participant2: ParticipantResponse;
    addVote: (participantId: string) => void;
}

interface VoteCardProps {
    participant: ParticipantResponse
}

function VoteCard(props: VoteCardProps) {
    function parseTwitterHandle(): string {
        const handle = props.participant.twitterHandle;

        if (handle.startsWith("@")) {
            const split = handle.split("@");
            return split[1];
        } else if (handle.startsWith("https://") || handle.startsWith("http://") || handle.startsWith("www") || handle.startsWith("twitter.com")) {
            const split = handle.split("/")
            return split[split.length - 1];
        } else {
            return handle;
        }
    }

    function renderBio() {
        if (props.participant.bio && props.participant.bio.length > 0) {
            return (
                <div className="w-full bg-white rounded bg-opacity-10 mb-2 py-2 px-2">
                    <p className="text-3xs opacity-50 font-mono uppercase">{props.participant.bio}</p>
                </div>
            )
        }
    }

    function getPreviewUrl(): string {
        // @ts-ignore
        const uuid = serialToImageNameJson[props.participant.azukiId];
        return `https://azk.imgix.net/images/${uuid}.png?fm=jpg&w=800`;
    }

    return (
        <div className={`rounded-2xl lg:h-[700px] md:h-[600px] h-[27rem] overflow-hidden grid-cols-1 ${textColor(props.participant.backgroundTrait)}`} style={{background: `${getBackgroundColor(props.participant.backgroundTrait)} none repeat scroll 0% 0%`}}>
            <div className="col-span-6 square grid-cols-1 relative">
                <img className="lg:w-full overlay-item mx-auto square" src={getPreviewUrl()} alt='' />
            </div>

            <div className="relative col-span-6 px-0 lg:px-0 py-0 lg:py-0 flex-col w-full flex justify-center content-start">
                <div className="grid-cols-1">
                    <div className="overlay-item flex flex-col z-50" style={{opacity: 1, transform: `translate3d(0%, 0%, 0px)`}}>
                        <div className="mt-2 w-full bg-white rounded bg-opacity-10 mb-2 py-2 px-4">
                            <p className="text-3xs opacity-50 font-mono uppercase">@{parseTwitterHandle()}</p>
                        </div>
                    </div>
                    <div className="overlay-item flex flex-col z-50" style={{opacity: 1, transform: `translate3d(0%, 0%, 0px)`}}>
                        {renderBio()}
                    </div>
                </div>
            </div>
        </div>
    )
}

function VoteWindow(props: Props) {
    function vote(e: React.MouseEvent<HTMLButtonElement, MouseEvent>, id: string): void {
        e.preventDefault();

        if (id === "participant1") {
            props.addVote(props.participant1.id);
        } else if (id === "participant2") {
            props.addVote(props.participant2.id);
        }
    }

    // TODO: Add onLoad for both card images to add a loading bar
    return (
        <div className="flex">
            <div className="pr-2 w-1/2">
                <button id="participant1" onClick={e => vote(e, "participant1")}>
                    <VoteCard participant={props.participant1} />
                </button>
            </div>

            <div className="pl-2 w-1/2">
                <button id="participant2" onClick={e => vote(e, "participant2")}>
                    <VoteCard participant={props.participant2} />
                </button>
            </div>
        </div>
    );
}

export default VoteWindow;
