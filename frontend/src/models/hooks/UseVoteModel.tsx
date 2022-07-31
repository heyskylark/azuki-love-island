import BracketGroup, { BracketGroupCreator } from "../api/BracketGroup";
import ParticipantResponse from "../api/ParticipantResponse";

export interface DenormalizedBracketGroup {
    submission1: ParticipantResponse;
    submission2?: ParticipantResponse;
    sortOrder: number;
}

export interface UseVoteResponse {
    state: VoteStateEnum;
    seasonNumber: number;
    currentRoundNumber: number;
    finalRoundNumber: number;
    startDate: Date;
    deadline: Date;
    remainingVotes: number;
    undoDisabled: boolean;
    canClaimPoap: boolean;
    twitterHandle?: string;
    currentVoteGroup?: DenormalizedBracketGroup;
    undo: () => void;
    vote: (participantId: string) => void;
    nextRoundDate: () => Date | undefined,
    registerTwitterHandle: (handle: string) => Promise<void>;
}

export enum VoteStateEnum {
    LOADING,
    REGISTER,
    VOTING_NOT_STARTED,
    VOTING_ENDED,
    VOTING_FEMALE,
    VOTING_MALE,
    FINISHED_VOTING
}

export interface VoteState {
    state: VoteStateEnum;
    seasonNumber: number;
    votedAtLeastOnce: boolean;
    roundVotingFinished: boolean;
    startDate: Date;
    deadline: Date;
    voteGapTimeMilli: number;
    currentRoundNumber: number;
    finalRoundNumber: number;

    maleVoteIndex: number;
    femaleVoteIndex: number;

    initFemaleRoundGroups: BracketGroup[];
    initMaleRoundGroups: BracketGroup[];

    newRoundGroup: BracketGroupCreator;
    femaleVoteSubmission: BracketGroup[];
    maleVoteSubmission: BracketGroup[];

    participants: Map<string, ParticipantResponse>;
    initialized: boolean;
    twitterHandle?: string;
}

export type VoteAction =
    | { type: "set-mode", state: VoteStateEnum }
    | { type: "set-dates", deadline: Date, startDate: Date, voteGapTimeMilli: number }
    | { type: "set-season-number", seasonNumber: number }
    | { type: "set-final-round", finalRoundNumber: number }
    | { type: "set-curr-round", currentRoundNumber: number }
    | { type: "set-participants", participants: Map<string, ParticipantResponse> }
    | { type: "set-init-round-groups", femaleInitRoundGroups: BracketGroup[], maleInitRoundGroups: BracketGroup[] }
    | { type: "set-voted-once", votedAtLeastOnce: boolean }
    | { type: "set-round-voting-finished", roundVotingFinished: boolean }
    | { type: "register", handle: string }
    | { type: "vote-female", voteGroup: BracketGroup }
    | { type: "vote-male", voteGroup: BracketGroup }
    | { type: "remove-vote-female" }
    | { type: "remove-vote-male" }
    | { type: "increment-male-index" }
    | { type: "increment-female-index" }
    | { type: "decrement-male-index" }
    | { type: "decrement-female-index" }
    | { type: "reset-male-index" }
    | { type: "reset-female-index" }
    | { type: "update-round-group", roundGroup: BracketGroupCreator }
    | { type: "reset" }
    | { type: "init" }
    ;
