import GenderedRoundWinners from "../../models/api/GenderedRoundWinners";
import ParticipantResponse from "../../models/api/ParticipantResponse";
import ResultsFilterState from "../../models/ResultsFilterState";
import SmallImagePreview from "../SmallImagePreview";

interface VSProps {
    participant1: string;
    participant2: string;
    participants: Map<String, ParticipantResponse>;
}

function VSGroup(props: VSProps) {
    const participant1 = props.participants.get(props.participant1);
    const participant2 = props.participants.get(props.participant2);

    if (participant1 && participant2) {
        return (
            <>
                <SmallImagePreview className="p-2 w-1/2 lg:w-1/6" vs={true} azukiId={participant1.azukiId} twitterHandle={participant1.twitterHandle} />
                <SmallImagePreview className="p-2 w-1/2 lg:w-1/6" azukiId={participant2.azukiId} twitterHandle={participant2.twitterHandle} />
            </>
        );
    } else {
        return <></>;
    }
}

interface Props {
    results: GenderedRoundWinners;
    filterState: ResultsFilterState;
    participants: Map<String, ParticipantResponse>;
    finalRound: number;
}

function RoundResults(props: Props) {
    function getRoundNumber(): string {
        if (props.finalRound === props.results.roundNumber) {
            return "Winner";
        } else {
            return `Round ${props.results.roundNumber}`;
        }
    }

    function getGridSize(numOfGroups: number): string {
        if (numOfGroups > 1) {
            return "grid-cols-2 lg:grid-cols-4 justify-items-center"
        } else {
            return "grid-cols-2 lg:grid-cols-1 justify-items-center"
        }
    }
    
    function renderVs() {
        const winners = (props.filterState === ResultsFilterState.FEMALE) ? props.results.femaleWinners : props.results.maleWinners

        let preview: JSX.Element[] = []
        winners.forEach(participant => {
            if (participant.submissionId2) {
                preview.push(
                    <VSGroup
                        key={`${participant.sortOrder}.${participant.submissionId1}.${participant.submissionId2}.${Math.random()}`}
                        participant1={participant.submissionId1}
                        participant2={participant.submissionId2}
                        participants={props.participants}
                    />
                )
            } else {
                const singleParticipant = props.participants.get(participant.submissionId1);

                if (singleParticipant) {
                    preview.push(
                        <SmallImagePreview
                        key={`${singleParticipant.azukiId}.${Math.random()}`}
                            className="w-full lg:w-1/3"
                            azukiId={singleParticipant.azukiId}
                            twitterHandle={singleParticipant.twitterHandle}
                        />
                    )
                }
            }
        });

        return (
            //"grid grid-cols-2 gap-x-4 gap-y-1 lg:grid-cols-6 lg:gap-x-6 lg:gap-y-2 lg:col-span-3 2xl:grid-cols-6"
            <div className="flex flex-wrap justify-center">
                {preview}
            </div>
        );
    }

    return (
        <div>
            <div className={`w-full mx-2 mb-4 border-b-2 border-gray-100 ${(props.finalRound === props.results.roundNumber) ? "text-center" : ""}`}>
                <h1 className="mb-3 uppercase font-black text-2xl lg:text-3xl whitespace-pre-line">{getRoundNumber()}</h1>
            </div>

            <div className="flex">
                <div className="w-full">
                    {renderVs()}
                </div>
            </div>
        </div>
    )
}

export default RoundResults;
