interface Props {
    text: string
}

function VoteFinished(props: Props) {
    return (
        <div className="w-full flex flex-wrap justify-center lg:text-center text-center">
            <div className="w-full lg:w-1/2 ">
                <img className="mb-2" src="images/girl.png" alt='' />
                <h1 className="mb-6 uppercase font-black text-3xl lg:text-4xl whitespace-pre-line">{props.text}</h1>
            </div>
        </div>
    );
}

export default VoteFinished;
