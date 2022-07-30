import { AxiosError } from "axios";
import { useEffect, useReducer } from "react";
import { toast } from "react-toastify";
import { getLatestVoteBracket, getLatestVoteBracketByTwitterHandle, getSeasonParticipants, getSeasonsInitialBracket, voteOnGenderedBracket } from "../clients/MainClient";
import BracketGroup from "../models/api/BracketGroup";
import GenderedInitialBracket from "../models/api/GenderedInitialBracket";
import ParticipantResponse from "../models/api/ParticipantResponse";
import VoteRequest from "../models/api/VoteRequest";
import { DenormalizedBracketGroup, UseVoteResponse, VoteAction, VoteState, VoteStateEnum } from "../models/hooks/UseVoteModel";

const initialVoteState: VoteState = {
    state: VoteStateEnum.LOADING,
    seasonNumber: 0,
    votedAtLeastOnce: false,
    roundVotingFinished: false,
    startDate: new Date(),
    deadline: new Date(),
    voteGapTimeMilli: -1,
    currentRoundNumber: 0,
    finalRoundNumber: 0,
    maleVoteIndex: 0,
    femaleVoteIndex: 0,
    initFemaleRoundGroups: [],
    initMaleRoundGroups: [],
    femaleVoteSubmission: [],
    maleVoteSubmission: [],
    newRoundGroup: {},
    participants: new Map(),
    initialized: false
}

// TODO: do not have to deliver the whole list to components only what the component needs to display
function useVote(): UseVoteResponse {
    const [voteState, voteDispatch] = useReducer(voteReducer, initialVoteState);

    useEffect(() => {
        async function initData() {
            try {
                const initialRoundResponse = await getSeasonsInitialBracket();
                const initialRound = initialRoundResponse.data;

                voteDispatch({ type: "set-dates", deadline: new Date(initialRound.voteDeadline), startDate: new Date(initialRound.voteStartDate), voteGapTimeMilli: initialRound.voteGapTimeMilliseconds ? initialRound.voteGapTimeMilliseconds : -1 });
                voteDispatch({ type: "set-season-number", seasonNumber: initialRound.seasonNumber });
                voteDispatch({ type: "set-final-round", finalRoundNumber: initialRound.numOfBrackets });

                // TODO: LatestVoteRound must return if voted ever and should not bail if never voted
                const latestVoteRoundResponse = await getLatestVoteBracket();
                const latestVoteRound = latestVoteRoundResponse.data;

                if (latestVoteRound.twitterHandle) {
                    voteDispatch({ type: "register", handle: latestVoteRound.twitterHandle });
                }

                voteDispatch({ type: "set-curr-round", currentRoundNumber: Math.min(latestVoteRound.roundNumber + 1, initialRound.numOfBrackets) });
                voteDispatch({ type: "set-voted-once", votedAtLeastOnce: latestVoteRound.hasVoted });
                voteDispatch({ type: "set-round-voting-finished", roundVotingFinished: latestVoteRound.finishedVoting });

                const votingClosed = votingNotStarted(initialRound) || votingEnded(initialRound);

                if (!votingClosed) {
                    await fetchParticipants(initialRound.seasonNumber);

                    voteDispatch({ type: "set-init-round-groups", femaleInitRoundGroups: latestVoteRound.femaleBracketGroups, maleInitRoundGroups: latestVoteRound.maleBracketGroups })
                }

                voteDispatch({ type: "init" });
            } catch (err) {
                if (err instanceof AxiosError && err.response) {
                    const errMessage = err.response?.data?.message ? err.response?.data?.message : "There was a problem getting the vote info.";
                    toast.error(errMessage);
                } else {
                    console.log(err);
                    toast.error("There was a problem getting the vote info.");
                }

                voteDispatch({ type: "reset" });
            }
        }

        initData();
    }, [])

    useEffect(function checkVotingState() {
        if (voteState.initialized) {
            const now = new Date().getTime();

            if (now > voteState.deadline.getTime()) {
                voteDispatch({ type: "set-mode", state: VoteStateEnum.VOTING_ENDED })
            } else if (now < voteState.startDate.getTime()) {
                voteDispatch({ type: "set-mode", state: VoteStateEnum.VOTING_NOT_STARTED }) 
            } else if (!voteState.twitterHandle) {
                voteDispatch({ type: "set-mode", state: VoteStateEnum.REGISTER }) 
            } else if (voteState.roundVotingFinished) {
                voteDispatch({ type: "set-mode", state: VoteStateEnum.FINISHED_VOTING }) 
            } else {
                voteDispatch({ type: "set-mode", state: VoteStateEnum.VOTING_FEMALE }) 
            }
        }
    }, [voteState.roundVotingFinished, voteState.deadline, voteState.startDate, voteState.twitterHandle, voteState.initialized])

    async function fetchParticipants(seasonNumber: number) {
        const seasonsParticipantsResponse = await getSeasonParticipants(seasonNumber);
        const seasonsParticipants = seasonsParticipantsResponse.data.participants;
    
        const tempMap = new Map<string, ParticipantResponse>()
        seasonsParticipants.forEach(participant => {
            tempMap.set(participant.id, participant);
        });
    
        voteDispatch({ type: "set-participants", participants: tempMap });
    }

    function getNextRoundDate(): Date | undefined {
        if ((voteState.currentRoundNumber) < voteState.finalRoundNumber && voteState.voteGapTimeMilli > -1) {
            const nextRoundMilli = voteState.startDate.getTime() + (voteState.voteGapTimeMilli * (voteState.currentRoundNumber))
            return new Date(nextRoundMilli);
        }
    }

    function getCurrentVoteGroup(): DenormalizedBracketGroup | undefined {
        let bracketGroup;
        if (voteState.state === VoteStateEnum.VOTING_FEMALE) {
            bracketGroup = voteState.initFemaleRoundGroups[voteState.femaleVoteIndex];
        } else if (voteState.state === VoteStateEnum.VOTING_MALE) {
            bracketGroup = voteState.initMaleRoundGroups[voteState.maleVoteIndex];
        }

        if (bracketGroup && bracketGroup.submissionId1 && bracketGroup.submissionId2) {
            const sub1 = voteState.participants.get(bracketGroup.submissionId1)
            const sub2 = voteState.participants.get(bracketGroup.submissionId2)

            if (sub1 && sub2) {
                return {
                    submission1: sub1,
                    submission2: sub2,
                    sortOrder: bracketGroup.sortOrder
                }
            }
        }
    }

    async function vote(participantId: string) {
        const newRoundGroup = voteState.newRoundGroup;
        const currVoteGroup = getCurrentVoteGroup();
        const state = voteState.state;

        if (!currVoteGroup || (currVoteGroup.submission1.id !== participantId && currVoteGroup.submission2?.id !== participantId)) {
            console.log(`Tried voting with participant id ${participantId}, with current vote group: `, currVoteGroup);
            toast.error("There was a problem voting, please refresh and try again.");
        }

        if (state === VoteStateEnum.VOTING_FEMALE || state === VoteStateEnum.VOTING_MALE) {
            // This for the final round where the vote is only done for one female and one male
            if ((voteState.currentRoundNumber === voteState.finalRoundNumber) && voteState.initFemaleRoundGroups.length === 1) {
                if (state === VoteStateEnum.VOTING_FEMALE) {
                    const group = {
                        submissionId1: participantId,
                        sortOrder: voteState.femaleVoteSubmission.length
                    }

                    voteDispatch({ type: "vote-female", voteGroup: group });
                    voteDispatch({ type: "update-round-group", roundGroup: {} });
                    voteDispatch({ type: "set-mode", state: VoteStateEnum.VOTING_MALE });
                } else {
                    const group = {
                        submissionId1: participantId,
                        sortOrder: voteState.maleVoteSubmission.length
                    }

                    voteDispatch({ type: "update-round-group", roundGroup: {} });
                    await sendVoteRequest([...voteState.maleVoteSubmission, group]);
                    return;
                }
            }

            if (!newRoundGroup.submissionId1) {
                voteDispatch({ type: "update-round-group", roundGroup: { submissionId1: participantId } });

                if (state === VoteStateEnum.VOTING_FEMALE) {
                    voteDispatch({ type: "increment-female-index" });
                } else if (state === VoteStateEnum.VOTING_MALE) {
                    voteDispatch({ type: "increment-male-index" });
                }
            } else if (!newRoundGroup.submissionId2) {
                if (state === VoteStateEnum.VOTING_FEMALE) {
                    if (voteState.femaleVoteIndex + 1 === voteState.initFemaleRoundGroups.length) {
                        const group: BracketGroup = {
                            submissionId1: newRoundGroup.submissionId1,
                            submissionId2: participantId,
                            sortOrder: voteState.femaleVoteSubmission.length
                        }

                        voteDispatch({ type: "vote-female", voteGroup: group });
                        voteDispatch({ type: "update-round-group", roundGroup: {} });
                        voteDispatch({ type: "set-mode", state: VoteStateEnum.VOTING_MALE });
                    } else {
                        voteDispatch({ type: "increment-female-index" });
                        voteDispatch({ type: "update-round-group", roundGroup: { ...voteState.newRoundGroup, submissionId2: participantId } });
                    }
                } else if (state === VoteStateEnum.VOTING_MALE) {
                    voteDispatch({ type: "increment-male-index" });

                    if (voteState.maleVoteIndex + 1 === voteState.initMaleRoundGroups.length) {
                        const group: BracketGroup = {
                            submissionId1: newRoundGroup.submissionId1,
                            submissionId2: participantId,
                            sortOrder: voteState.maleVoteSubmission.length
                        }

                        await sendVoteRequest([...voteState.maleVoteSubmission, group]);
                    } else {
                        voteDispatch({ type: "update-round-group", roundGroup: { ...voteState.newRoundGroup, submissionId2: participantId } });
                    }
                }
            } else {
                if (state === VoteStateEnum.VOTING_FEMALE) {
                    const group: BracketGroup = {
                        submissionId1: newRoundGroup.submissionId1,
                        submissionId2: newRoundGroup.submissionId2,
                        sortOrder: voteState.femaleVoteSubmission.length
                    }

                    voteDispatch({ type: "vote-female", voteGroup: group });
                } else {
                    const group: BracketGroup = {
                        submissionId1: newRoundGroup.submissionId1,
                        submissionId2: newRoundGroup.submissionId2,
                        sortOrder: voteState.maleVoteSubmission.length
                    }

                    voteDispatch({ type: "vote-male", voteGroup: group });
                }

                voteDispatch({ type: "update-round-group", roundGroup: { submissionId1: participantId } });
            }
        }
    }

    async function sendVoteRequest(maleVoteGroup: BracketGroup[]) {
        voteDispatch({ type: "set-mode", state: VoteStateEnum.LOADING });

        if (!voteState.twitterHandle) {
            toast.error("There was a problem voting, twitter handle missing.");
            voteDispatch({ type: "reset" });
            return;
        }

        const voteRequest: VoteRequest = {
            twitterHandle: voteState.twitterHandle,
            femaleBracketGroups: voteState.femaleVoteSubmission,
            maleBracketGroups: maleVoteGroup
        }

        try {
            await voteOnGenderedBracket(voteRequest);
            voteDispatch({ type: "set-mode", state: VoteStateEnum.FINISHED_VOTING });
        } catch (err) {
            if (err instanceof AxiosError && err.response) {
                const expectedErr = err.response?.data?.message;
                const printErr = expectedErr ? expectedErr : "There was a problem sending the vote."
                toast.error(printErr);
            } else {
                console.log(err);
                toast.error("There was a problem sending the vote.");
            }

            voteDispatch({ type: "reset" }); 
        }
    }

    function undo() {
        if (undoDisabled()) {
            return;
        }

        const state = voteState.state;
        const newGroup = voteState.newRoundGroup;

        if (state === VoteStateEnum.VOTING_FEMALE && (voteState.femaleVoteIndex > 0 || newGroup.submissionId1 || newGroup.submissionId2)) {
            if (newGroup.submissionId2) {
                // TODO: Might be a good idea to put index increment/decrement part of round group updates.
                voteDispatch({ type: "update-round-group", roundGroup: { submissionId1: newGroup.submissionId1 } });
                voteDispatch({ type: "decrement-female-index" });
            } else if (newGroup.submissionId1) {
                const prev = voteState.femaleVoteSubmission.pop();

                if (prev) {
                    voteDispatch({ type: "update-round-group", roundGroup: { submissionId1: prev.submissionId1, submissionId2: prev.submissionId2 } });
                } else {
                    voteDispatch({ type: "update-round-group", roundGroup: {} });
                }

                voteDispatch({ type: "remove-vote-female" });
            }
        } else if (state === VoteStateEnum.VOTING_MALE) {
            if (newGroup.submissionId2) {
                voteDispatch({ type: "update-round-group", roundGroup: { submissionId1: newGroup.submissionId1 } });
                voteDispatch({ type: "decrement-male-index" });
            } else if (newGroup.submissionId1) {
                const prev = voteState.maleVoteSubmission.pop();
                
                if (prev) {
                    voteDispatch({ type: "update-round-group", roundGroup: { submissionId1: prev.submissionId1, submissionId2: prev.submissionId2 } });
                } else {
                    voteDispatch({ type: "update-round-group", roundGroup: {} });
                }

                voteDispatch({ type: "remove-vote-male" });
            } else {
                const prev = voteState.femaleVoteSubmission.pop();

                voteDispatch({ type: "update-round-group", roundGroup: { submissionId1: prev?.submissionId1 } });
                voteDispatch({ type: "remove-vote-female" });
                voteDispatch({ type: "set-mode", state: VoteStateEnum.VOTING_FEMALE });
            }
        }
    }
    
    function undoDisabled(): boolean {
        const state = voteState.state;
        const newGroup = voteState.newRoundGroup;

        if (state === VoteStateEnum.VOTING_FEMALE) {
            return newGroup.submissionId1 === undefined && voteState.femaleVoteSubmission.length === 0;
        } else if (state === VoteStateEnum.VOTING_MALE) {
            return false;
        }

        return true;
    }

    async function registerTwitterHandle(handle: string) {
        voteDispatch({ type: "set-mode", state: VoteStateEnum.LOADING })
        
        try {
            const latestVoteRoundResponse = await getLatestVoteBracketByTwitterHandle(handle);
            const latestVoteRound = latestVoteRoundResponse.data;

            voteDispatch({ type: "set-voted-once", votedAtLeastOnce: latestVoteRound.hasVoted });
            voteDispatch({ type: "set-round-voting-finished", roundVotingFinished: latestVoteRound.finishedVoting });

            voteDispatch({ type: "register", handle: handle });
        } catch(err) {
            if (err instanceof AxiosError && err.response) {
                const errMessage = err.response?.data?.message ? err.response?.data?.message : "There was a problem registering your twitter handle right now.";
                toast.error(errMessage);
            } else {
                console.log(err);
                toast.error("There was a problem registering your twitter handle right now.");
            }

            voteDispatch({ type: "reset" });
        }
    }

    function getRemainingVotes(): number {
        if (voteState.state === VoteStateEnum.FINISHED_VOTING || voteState.state === VoteStateEnum.VOTING_ENDED) {
            return 0;
        } else {
            return (voteState.initFemaleRoundGroups.length - voteState.femaleVoteIndex) +
                (voteState.initMaleRoundGroups.length - voteState.maleVoteIndex);
        }
    }

    return {
        state: voteState.state,
        seasonNumber: voteState.seasonNumber,
        currentRoundNumber: voteState.currentRoundNumber,
        finalRoundNumber: voteState.finalRoundNumber,
        startDate: voteState.startDate,
        deadline: voteState.deadline,
        remainingVotes: getRemainingVotes(),
        undoDisabled: undoDisabled(),
        currentVoteGroup: getCurrentVoteGroup(),
        nextRoundDate: getNextRoundDate,
        undo: undo,
        vote: vote,
        registerTwitterHandle: registerTwitterHandle
    }
}

function voteReducer(state: VoteState, action: VoteAction): VoteState {
    switch (action.type) {
        case "set-mode": {
            return { ...state, state: action.state }
        }
        case "set-dates": {
            return { ...state, startDate: action.startDate, deadline: action.deadline, voteGapTimeMilli: action.voteGapTimeMilli }
        }
        case "set-season-number": {
            return { ...state, seasonNumber: action.seasonNumber }
        }
        case "set-final-round": {
            return { ...state, finalRoundNumber: action.finalRoundNumber }
        }
        case "set-curr-round": {
            return { ...state, currentRoundNumber: action.currentRoundNumber }
        }
        case "set-participants": {
            return { ...state, participants: action.participants }
        }
        case "set-init-round-groups": {
            return { ...state, initFemaleRoundGroups: action.femaleInitRoundGroups, initMaleRoundGroups: action.maleInitRoundGroups }
        }
        case "set-voted-once": {
            return { ...state, votedAtLeastOnce: action.votedAtLeastOnce }
        }
        case "set-round-voting-finished": {
            return { ...state, roundVotingFinished: action.roundVotingFinished }
        }
        case "register": {
            return { ...state, twitterHandle: action.handle }
        }
        case "vote-female": {
            return { ...state, femaleVoteSubmission: [...state.femaleVoteSubmission, action.voteGroup], femaleVoteIndex: state.femaleVoteIndex + 1 }
        }
        case "vote-male": {
            return { ...state, maleVoteSubmission: [...state.maleVoteSubmission, action.voteGroup], maleVoteIndex: state.maleVoteIndex + 1 }
        }
        case "remove-vote-female": {
            if (state.femaleVoteIndex - 1 >= 0) {
                const femaleVoteSubmission = state.femaleVoteSubmission
                return { ...state, femaleVoteSubmission: femaleVoteSubmission.slice(0, femaleVoteSubmission.length), femaleVoteIndex: state.femaleVoteIndex - 1 }
            } else {
                return state;
            }
        }
        case "remove-vote-male": {
            if (state.maleVoteIndex - 1 >= 0) {
                const maleVoteSubmission = state.maleVoteSubmission
                return { ...state, maleVoteSubmission: maleVoteSubmission.slice(0, maleVoteSubmission.length), maleVoteIndex: state.maleVoteIndex - 1 }
            } else {
                return state;
            }
        }
        case "update-round-group": {
            return { ...state, newRoundGroup: action.roundGroup }
        }
        case "increment-female-index": {
            return { ...state, femaleVoteIndex: state.femaleVoteIndex + 1 }
        }
        case "increment-male-index": {
            return { ...state, maleVoteIndex: state.maleVoteIndex + 1 }
        }
        case "decrement-female-index": {
            if (state.femaleVoteIndex - 1 >= 0) {
                return { ...state, femaleVoteIndex: state.femaleVoteIndex - 1 }
            } else {
                return state;
            }
        }
        case "decrement-male-index": {
            if (state.maleVoteIndex - 1 >= 0) {
                return { ...state, maleVoteIndex: state.maleVoteIndex - 1 }
            } else {
                return state;
            }
        }
        case "reset-female-index": {
            return { ...state, femaleVoteIndex: 0 }
        }
        case "reset-male-index": {
            return { ...state, maleVoteIndex: 0 }
        }
        case "reset": {
            return { ...state, state: VoteStateEnum.REGISTER, femaleVoteSubmission: [], maleVoteSubmission: [], newRoundGroup: {}, votedAtLeastOnce: false, roundVotingFinished: false, twitterHandle: undefined };
        }
        case "init": {
            return { ...state, initialized: true }
        }
        default:
            return state;
    }
}

function votingNotStarted(initialRound: GenderedInitialBracket): boolean {
    const nowMilli = new Date().getTime();

    return nowMilli < initialRound.voteStartDate;
}

function votingEnded(initialRound: GenderedInitialBracket): boolean {
    const nowMilli = new Date().getTime();

    return nowMilli > initialRound.voteDeadline;
}

export default useVote;
